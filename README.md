# Point.im telegram

pointim_bot is a telegram bot for point.im micro blogging service.

## Docker run instructions

1. Build or pull docker image:
	- `docker pull paskal/pointim_bot` or
	- `docker-compose build`

1. In `docker-compose.yml` file uncomment `volumes` and `environment` sections
	- `/root/cache.bin` must be replaced to some persistent folder noone else have access to on your server
	- `LOGIN` and `PASSWORD` are left and right [token parts](https://core.telegram.org/bots#6-botfather) of your bot

1. Run service with command `docker-compose up -d`, inspect it's state with `docker-compose logs`
