# Movie Ticket Booking System

## Requirements
1. Upon opening the application, user should be able to select the respective location
2. Based on the location, user should be able to see current movies running in the theatres
3. On Selecting any movie, app should display movie slot timings on respective day and t+10 days
4. Upon seat selections booking should get started and upon successful payment booking should be confirmed
5. Theatre Company Owners will publish the current running movies at each location in the application
6. No 2 users should get a same seat

## Actors Involved
1. User - Bookings, payments
2. ApplicationAdmin -  Responsible for maintaining application
3. TheatreOwners - Responsible for registering theatres and adding movies

## Entities Involved
1. Location
2. Movie
3. MovieSlot
4. Seat
5. TheatreHall
6. TheatreCompany
7. Booking
8. Payment

## High Level Design

Learn these things later

1. User Service
2. Ticket Service
3. Search Service
4. Payment Service
5. Movie Management Service

## API Design

### Note for all endpoints:
```
401 Invalid Token / API key
403 UnAuthorized
200 Success
400 bad request
429 Too Many Requests            # Rate limiting
503 Service Unavailable          # During maintenance
```
### User Service
Signup, signin, forgot password will be handled by aws cognito
```
# User Authentication (Cognito)

POST /v1/auth/signup              # User signup
POST /v1/auth/signin              # User signin (returns JWT)
POST /v1/auth/signout             # Signout (invalidates token)
GET  /v1/users/{id}               # User details
PUT  /v1/users/{id}               # Update user details

# Theatre Owner Management
POST /v1/owners                   # Register theatre owner 
GET  /v1/owners/{ownerId}         # Get owner profile
PUT  /v1/owners/{ownerId}         # Update owner details

# Admin Endpoints (Missing)
POST /v1/admin/cities             # Add new city
GET  /v1/admin/users              # List all users
DELETE /v1/admin/users/{userId}   # Deactivate user
```

### Ticket Service
```
# Shows & Bookings
POST /v1/shows                    # Create show (theatreId, movieId, hallId, timing, date) for theatre owners
GET  /v1/shows?movie={id}&date={YYYY-MM-DD} # Filter by date
POST /v1/shows/{showId}/reserve   # Reserve seats (body: seats[], timeout=15m) for users
PATCH /v1/bookings/{bookingId}    # Add/remove seats pre-payment
DELETE /v1/bookings/{bookingId}   # Cancel booking
GET /v1/users/{id}/bookings       # Booking history	
```

### MovieManagement Service
```
# Movie Management
POST /v1/theatres                 # Create Theatre (location, owner name, mobile number)
GET  /v1/theatres/{id}
PUT  /v1/theatres/{id}

# Movie Halls
POST /v1/moviehalls               # Create hall (body: theatreId, capacity, seats)
GET  /v1/moviehalls?theatre={id}  # List halls by theatre
PUT  /v1/moviehalls/{hallId}      # Update hall capacity/seats
DELETE /v1/moviehalls/{hallId}     # Remove hall

# Movie Metadata
POST /v1/movies       # Add new movies
PUT  /v1/movies/{id}  # Update movie details
```

### Search Service
```
# Movies
GET /v1/movies?city={id}&genre=action&lang=en&page=1&size=20
# Theatres
GET /v1/theatres?city={id}&lat=12.971&lon=77.594&radius=5km
# Auto-Complete (Missing)
GET /v1/search/suggestions?q=aveng&city={id}
```

### Payment Service
```
POST /v1/payments                 # Initiate payment (bookingId, amount)
PUT  /v1/payments/{txnId}         # Confirm/cancel payment
GET  /v1/payments/{txnId}          # Check status
```

## Middleware
- Upon every request, we need to perform user Authentication to fetch the current user who is performing the request.