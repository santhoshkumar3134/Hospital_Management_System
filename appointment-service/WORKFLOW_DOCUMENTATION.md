# Appointment Service - Modified Workflow Documentation

## Overview
Modified the Appointment Service to support a **3-step booking workflow** where patients first select a specialization, then a doctor, and finally a time slot.

---

## Workflow Steps

### Step 1: Get Available Doctors by Specialization
**Endpoint:** `POST /api/v1/appointments/available-doctors`

**Request Body:**
```json
{
  "patientId": 123,
  "specialization": "Cardiology"
}
```

**Response:**
```json
[
  {
    "doctorId": 456,
    "doctorName": "Dr. Smith",
    "specialization": "Cardiology",
    "availableSlots": [
      {
        "slotId": 789,
        "doctorId": 456,
        "startTime": "2026-04-01T10:00:00",
        "available": true
      },
      {
        "slotId": 790,
        "doctorId": 456,
        "startTime": "2026-04-01T10:30:00",
        "available": true
      }
    ]
  },
  {
    "doctorId": 457,
    "doctorName": "Dr. Johnson",
    "specialization": "Cardiology",
    "availableSlots": [...]
  }
]
```

**What it does:**
- Patient provides their ID and desired specialization
- Appointment Service queries DoctorSchedule service for available doctors
- Returns list of doctors with their available time slots
- Each slot has 30-minute duration (fixed by design)

---

### Step 2: Get Available Time Slots for Selected Doctor
**Endpoint:** `POST /api/v1/appointments/available-slots`

**Request Body:**
```json
{
  "patientId": 123,
  "doctorId": 456
}
```

**Response:**
```json
[
  {
    "slotId": 789,
    "doctorId": 456,
    "startTime": "2026-04-01T10:00:00",
    "available": true
  },
  {
    "slotId": 790,
    "doctorId": 456,
    "startTime": "2026-04-01T10:30:00",
    "available": true
  },
  {
    "slotId": 791,
    "doctorId": 456,
    "startTime": "2026-04-01T11:00:00",
    "available": true
  }
]
```

**What it does:**
- Patient has selected a doctor, now requests their available slots
- Appointment Service queries DoctorSchedule service for doctor-specific slots
- Returns detailed time slots with 30-minute intervals
- Patient can now choose their preferred time slot

---

### Step 3: Book Appointment
**Endpoint:** `POST /api/v1/appointments/book`

**Request Body:**
```json
{
  "patientId": 123,
  "doctorId": 456,
  "slotId": 789
}
```

**Response:**
```json
{
  "appointmentId": 1000,
  "patientId": 123,
  "doctorId": 456,
  "timeSlotId": 789,
  "appointmentDate": null,
  "status": "CONFIRMED",
  "createdAt": "2026-03-31T15:30:00"
}
```

**What it does:**
- Patient confirms their appointment selection
- Appointment Service performs concurrency checks (Optimistic Locking via @Version)
- Calls DoctorSchedule service to claim the specific time slot
- Persists the appointment locally
- Returns confirmed appointment details
- Handles concurrent booking attempts with @Version field

---

## DTOs Created/Modified

### New DTOs:
1. **GetAvailableDoctorsRequest** - Request for Step 1
   - `patientId`: Long
   - `specialization`: String

2. **GetTimeSlotsRequest** - Request for Step 2
   - `patientId`: Long
   - `doctorId`: Long

3. **BookAppointmentRequest** - Request for Step 3
   - `patientId`: Long
   - `doctorId`: Long
   - `slotId`: Long

4. **DoctorAvailabilityDTO** - Response for Step 1
   - `doctorId`: Long
   - `doctorName`: String
   - `specialization`: String
   - `availableSlots`: List<TimeSlotDTO>

5. **TimeSlotDTO** - Time slot details
   - `slotId`: Long
   - `doctorId`: Long
   - `startTime`: LocalDateTime (30-min fixed duration)
   - `available`: boolean

---

## Feign Client Updates (`DoctorServiceClient`)

New methods added for DoctorSchedule service communication:

```java
// Get available doctors by specialization
@GetMapping("/api/v1/doctor-schedule/doctors/specialization/{specialization}")
List<DoctorAvailabilityDTO> getAvailableDoctorsBySpecialization(String specialization);

// Get available time slots for a specific doctor
@GetMapping("/api/v1/doctor-schedule/slots/doctor/{doctorId}")
List<TimeSlotDTO> getTimeSlotsByDoctorId(Long doctorId);

// Claim a specific time slot
@PutMapping("/api/v1/doctor-schedule/slots/claim/{slotId}")
void claimTimeSlot(Long slotId, Long patientId);
```

---

## Model Updates (`Appointment`)

Added new field:
```java
@Column(name = "time_slot_id")
private Long timeSlotId;  // Links to the specific 30-minute slot booked
```

---

## Service Layer (`AppointmentService`)

Three new methods:

1. **getAvailableDoctorsBySpecialization()** - Calls DoctorSchedule for doctors
2. **getTimeSlotsForDoctor()** - Calls DoctorSchedule for specific doctor's slots
3. **bookAppointment(BookAppointmentRequest)** - Performs concurrency checks and books slot

All methods include error handling for remote service failures.

---

## Repository Updates (`AppointmentRepository`)

Added custom query method:
```java
boolean existsByPatientIdAndDoctorId(Long patientId, Long doctorId);
```

Prevents duplicate appointments between same patient-doctor pair.

---

## Key Features

✅ **Three-step workflow** - Specialization → Doctor → Time Slot
✅ **Optimistic Locking** - @Version field prevents double-bookings under high concurrency
✅ **30-minute fixed slots** - TimeSlot only stores startTime, duration is implicit
✅ **Spring Cloud OpenFeign** - Seamless DoctorSchedule service integration
✅ **Global Exception Handling** - Consistent error responses
✅ **Backward Compatibility** - Legacy `/book-legacy` endpoint still supported

---

## DoctorSchedule Service Expectations

The DoctorSchedule microservice should provide these endpoints:

```
GET  /api/v1/doctor-schedule/doctors/specialization/{specialization}
     → Returns: List<DoctorAvailabilityDTO>

GET  /api/v1/doctor-schedule/slots/doctor/{doctorId}
     → Returns: List<TimeSlotDTO>

PUT  /api/v1/doctor-schedule/slots/claim/{slotId}?patientId=123
     → Marks slot as unavailable for the patient
```

---

## Configuration Required

Add to `application.properties`:
```properties
doctor.service.url=http://doctor-service:8080
```

Or via Eureka service discovery:
```properties
doctor.service.url=http://doctor-service
```
