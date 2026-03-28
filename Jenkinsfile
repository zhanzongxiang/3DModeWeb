node {
    stage("Clone") {
        git branch: "main",
            credentialsId: "github-token",
            url: "https://github.com/zhanzongxiang/3DModeWeb.git"
    }
    stage("Build Backend") {
        sh "cd backend && docker build -t modelhub-backend:latest ."
    }
    stage("Deploy Backend") {
        sh "docker rm -f modelhub-backend || true"
        sh """
docker run -d --name modelhub-backend --network host \
  -e DB_HOST=127.0.0.1 \
  -e DB_PORT=3306 \
  -e DB_NAME=model_hub \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e REDIS_HOST=127.0.0.1 \
  -e REDIS_PORT=6379 \
  -e OSS_ENDPOINT=http://127.0.0.1:9000 \
  -e OSS_REGION=us-east-1 \
  -e OSS_ACCESS_KEY=minioadmin \
  -e OSS_SECRET_KEY=minioadmin \
  -e OSS_BUCKET=model-hub \
  -e OSS_PUBLIC_BASE_URL=http://localhost:9000/model-hub \
  -e JWT_SECRET=change-this-to-a-long-random-secret-at-least-32-bytes \
  -e JWT_EXPIRATION_SECONDS=7200 \
  --restart unless-stopped \
  modelhub-backend:latest
        """
    }
}
