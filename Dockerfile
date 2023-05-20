FROM amazoncorretto:17-alpine3.17-jdk
WORKDIR /server
CMD chmod -R 777 /server && \
    echo "eula=true" >> /server/eula.txt && \
    wget "https://api.papermc.io/v2/projects/paper/versions/1.19.2/builds/307/downloads/paper-1.19.2-307.jar" -O "/server/paper.jar" && \
    echo "Docker Papermc Server is Starting" && \
    chmod -R 777 /server && \
    java -Xmx3G -jar /server/paper.jar 