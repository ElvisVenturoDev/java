# MySQL en Docker para Miskylac

## 1. Crear el contenedor (una sola vez)
docker run -d --name miskylac-mysql -p 3308:3306 -e MYSQL_ROOT_PASSWORD=Root2026 -e MYSQL_DATABASE=Miskylac -e MYSQL_USER=santa_clara -e MYSQL_PASSWORD=SantaClara2026 -v miskylac_data:/var/lib/mysql --restart unless-stopped mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

## 2. Crear las tablas (espera ~20 s a que MySQL arranque; funciona igual en CMD, PowerShell y Bash)
docker cp esquema.sql miskylac-mysql:/esquema.sql
docker exec miskylac-mysql sh -c "mysql -uroot -pRoot2026 < /esquema.sql"

## 3. Workbench -> nueva conexión
Hostname: 127.0.0.1   Port: 3308   Username: santa_clara   Password: SantaClara2026   Default Schema: Miskylac
