# System Design: Video Upload & Conversion Platform

This project is a microservices-based system for uploading, converting, and managing video files. It includes authentication, video upload, conversion, and notification services, all communicating via REST APIs and RabbitMQ.

## Architecture Overview

- **auth-service**: Handles user registration, authentication, and authorization (JWT-based). User data is stored in **PostgreSQL**.
- **upload-service**: Accepts video uploads, stores them in MongoDB GridFS, and sends processing requests to the conversion service.
- **conversion-service**: Listens for video processing requests, converts videos to MP3 using FFmpeg, and stores audio in GridFS.
- **notification-service**: Sends email notifications to users.

All services are containerized and communicate over HTTP and RabbitMQ.

---

## Workflow

1. **User Registration & Login**
   - Users register and log in via the `auth-service`.
   - User credentials and roles are stored in **PostgreSQL**.
   - JWT tokens are issued for authenticated sessions.

2. **Video Upload**
   - Authenticated users upload videos via the `upload-service`.
   - Videos are stored in MongoDB GridFS.
   - A message is sent to the `conversion-service` via RabbitMQ for processing.

3. **Video Conversion**
   - The `conversion-service` receives the message, fetches the video, converts it to MP3 using FFmpeg, and stores the audio in GridFS.

4. **Notification**
   - Upon successful conversion, the `notification-service` can send an email to the user.

---

## Main API Endpoints

### Auth Service

- `POST /register` — Register a new user.
- `POST /login` — Authenticate and receive a JWT.
- `GET /validate` — Validate a JWT.
- `GET /users` — List all users (admin only).
- `DELETE /users?User-Id={id}` — Delete a user (admin only).

### Upload Service

- `POST /video` — Upload a video file (requires `User-Id` header).
- `GET /video/{fileId}` — Download a video by ID.
- `DELETE /admin/video/{fileId}` — Delete a video (admin only).
- `GET /admin/debug/files` — List all files in GridFS (admin/debug).

### Conversion Service

- Listens for video processing messages via RabbitMQ.
- Converts videos to MP3 and stores them in GridFS.

### Notification Service

- Sends emails to users (used internally).

---

## Technologies Used

- Java, Spring Boot
- **PostgreSQL** (for user storage in auth-service)
- MongoDB (GridFS)
- RabbitMQ
- FFmpeg
- Docker

---

## Running the System

1. Start **PostgreSQL**, MongoDB, and RabbitMQ.
2. Build and run each service (auth, upload, conversion, notification).
3. Use the API endpoints to register, upload videos, and manage files.

---

## Notes

- All endpoints requiring authentication expect a `Bearer` token in the `Authorization` header.
- Only admin users can access user management and video deletion endpoints.
- Video uploads are validated for type and size.
