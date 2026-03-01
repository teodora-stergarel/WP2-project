# SkillSwap — Credit-Based Student Learning Platform

### Team: Stergarel Teodora, Rus Alexandra  
- Group: 1231A  
- Subgroup: 2  

---

## Project Description


SkillSwap is a web application where students can offer and request tutoring sessions using a virtual credit system. Each new user receives a 5-credit welcome bonus. Lessons are priced between 1 and 5 credits, and credits are transferred between users when a lesson is requested.

The platform includes authentication, user roles, profile management, file upload, and Docker deployment.

---

## Tech Stack
- Backend: Spring Boot
- Security: Spring Security (encrypted passwords)
- Database: MySQL
- Frontend: Bootstrap (responsive UI)
- Deployment: Docker + docker-compose

---

## Core Features

### Authentication
- Secure registration and login using Spring Security
- Passwords are encrypted (BCrypt)

### Roles
- CLIENT (default role)
- ADMIN

### Profile Page
- View and edit personal information
- Upload profile picture
- View current credit balance

### Credit System
- Each new user receives 5 credits upon registration
- Lessons are priced between 1–5 credits
- When a lesson is requested, credits are transferred from requester to lesson owner
- Users cannot request a lesson if they do not have enough credits

### Tutoring Offers
- Create tutoring offers with:
  - title
  - description
  - credit price (1–5)
  - optional attachment
- Browse all offers
- View offer details
- Edit/delete own offers

### Lesson Requests
- Request a lesson
- View history of requested lessons

### Admin Features
- View all users
- View all offers
- Delete inappropriate offers

---

## User Stories

1) Authentication
- As a user, I want to register and log in securely, so that my account is protected and I can access private features.

2) Credit-Based Learning
- As a user, I want to receive 5 credits when I create an account, so that I can immediately start learning or teaching. Further promoting free education for students.

3) Create and Request Lessons
- As a user, I want to create tutoring offers and price them between 1 and 5 credits, so that others can request sessions.
- As a user, I want to request a lesson and pay with credits, so that I can learn from other students.

4) Admin Moderation
- As an admin, I want to manage users and delete inappropriate offers, so that the platform remains safe, usable and appropriate for students.

---

## Running with Docker
