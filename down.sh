echo "Stopping spring-boot-app system ..."
docker-compose down -v
docker system prune -f
docker ps -a
echo "spring-boot-app system stopped"
