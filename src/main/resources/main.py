#!/usr/bin/env python
import time
import json
import httplib
import urllib

tokenfile = open('bot.key', 'r')
token = tokenfile.readline().strip()
tokenfile.close()

host = 'api.telegram.org'
url = '/bot' + token + '/'

pointhost = 'point.im'
pointurl = '/api/'

ptoken = {}

conn = httplib.HTTPSConnection(host)

def pointApi(rtype, method, chatid, params, headers={}):
	pointconn = httplib.HTTPConnection(pointhost)
	postparams = urllib.urlencode(params)
	if chatid in ptoken.keys():
		headers['Authorization'] = ptoken[chatid][0]
	pointconn.request(rtype, pointurl + method, postparams, headers)
	res = pointconn.getresponse()
	body = res.read()
	pointconn.close()
	print 'from point api: ' + body
	return body

def sendBack(user, chatid, msg):
	print 'Sending to %s %s:\n%s' % (user, chatid, msg)
	postparams = urllib.urlencode({'chat_id':chatid, 'text':urllib.quote(msg.encode('utf8'))})
	#conn.request('POST', url+'sendMessage', postparams)
	conn.request('GET', url+'sendMessage?chat_id=%s&text=%s' % (chatid, urllib.quote(msg.encode('utf8'))))
	res = conn.getresponse()
	res.read()
	if res.status != 200:
		print 'Unable to send message to %s! %s' % (chatid, res.reason)
	else:
		print 'Sent to %s %s: %s' % (user, chatid, msg)
	pass

def startCmd(user, chatid, text):
	sendBack(user, chatid, 'Hello! This is bot for micro blog service point.im. Type /help for more commands.')
	pass

def helpCmd(user, chatid, text):
	sendBack(user, chatid, 'Support commands:\nlogin\nlogout\nrecent\npost\n')
	pass

def pingNonCmd(user, chatid, text):
	sendBack(user, chatid, 'pong')
	pass

def loginNonCmd(user, chatid, text):
	texts = text.split(' ')
	if len(texts) < 3:
		return sendBack(user, chatid, 'Not enought parameters in login command! Usage: login user password')
	login = texts[1]
	password = texts[2]
	pmsg = json.loads(pointApi('POST', 'login', chatid, {'login':login, 'password':password}))
	if not all(key in pmsg for key in ('token', 'csrf_token')):
		sendBack(user, chatid, 'Unable login!' + str(pmsg))
	else:
		ptoken[chatid] = (pmsg['token'], pmsg['csrf_token'])
		sendBack(user, chatid, 'logged in')
	pass

def logoutNonCmd(user, chatid, text):
	if chatid not in ptoken.keys():
		return sendBack(user, chatid, 'first you need to logon!')
	pmsg = json.loads(pointApi('POST', 'logout', chatid, {'csrf_token':ptoken[chatid][1]}))
	sendBack(user, chatid, str(pmsg))
	pass

def recentNonCmd(user, chatid, text):
	if chatid not in ptoken.keys():
		return sendBack(user, chatid, 'first you need to logon!')
	pmsg = json.loads(pointApi('GET', 'recent', chatid, {}))
	if 'error' in pmsg:
		return sendBack(user, chatid, 'Error: ' + str(pmsg['error']))
	
	if 'posts' not in pmsg:
		return

	for post in pmsg['posts']:
		body = '[%s] %s\n%s\n' % (post['post']['author']['login'], ", ".join((post['post']['tags'])), post['post']['text'])
		sendBack(user, chatid, body)
	pass

def createPostNonCmd(user, chatid, text):
	if chatid not in ptoken.keys():
		return sendBack(user, chatid, 'first you need to logon!')
	postmsg = text[4:].lstrip().encode('utf8')
	tags = []
	for word in postmsg.split(' '):
		if len(word) > 0 and  '*' != word[0]:
			break
		if len(word) > 0:
			tags.append(word)
			postmsg = postmsg.replace(word,"",1)

	pmsg = json.loads(pointApi('POST', 'post', chatid, {u'text':postmsg, u'tag':u' '.join(tags)}, {u'X-CSRF':ptoken[chatid][1]}))
    
	sendBack(user, chatid, str(pmsg))
	pass

commands = { '/start' : startCmd, '/help' : helpCmd }
noncommands = { 'ping': pingNonCmd, 'login':loginNonCmd, 'logout':logoutNonCmd, 
		'recent':recentNonCmd, 'post':createPostNonCmd, 'help':helpCmd, '?':helpCmd }

def newmessage(user, chatid, text):
	print 'message from %s %s: %s' % (user, chatid, text)
	if len(text) == 0:
		return

	if text[0] == '/': #command message
		cmd = text.split(' ')[0]
		if cmd in commands.keys():
			commands[cmd](user, chatid, text)
		else:
			print 'unknown command: ' + cmd
			sendBack(user, chatid, 'unknown command: '+cmd)
	else:
		noncmd = text.split(' ')[0]
		if noncmd in noncommands:
			noncommands[noncmd](user, chatid, text)
		else:
			print 'unknown non command: ' + noncmd
			sendBack(user, chatid, 'unknown noncommand: '+noncmd)
			
	

def main():
	updateid = 0
	while True:
		conn.request('GET', url+'getUpdates?offset=' + str(updateid))
		res = conn.getresponse()

		if res.status == 200:
			msgbody = res.read()
			msg = json.loads(msgbody)
			if msg['ok']:
				for res in msg['result']:
					updateid = res['update_id'] + 1
					print 'last update id:' + str(updateid)
					if 'message' in res.keys():
						message = res['message']
						if 'text' in message.keys():
							newmessage(message['from']['first_name'], message['chat']['id'] , message['text']);
						else:
							print 'nontext message from %s: %s' % (message['from']['first_name'], message)
					else:
						print 'unknown message type: %s' % res

		time.sleep(1)
		
if __name__ == "__main__":
	    main()

