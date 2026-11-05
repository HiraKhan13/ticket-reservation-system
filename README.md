# Dynamic Ticket Reservation & Cancellation System

A Java desktop application that simulates a ticket booking system for a venue. It books and cancels seats across three categories (VIP, Premium and Economy), and uses AVL Trees, HashMaps and Stacks to keep every operation fast.

This is a term project from my BS Software Engineering degree at the University of Central Punjab (UCP). The report and presentation are in [`docs/`](docs/).

## Features

- Book and cancel individual seats.
- Auto-expiry: bookings that are not confirmed are released after 5 minutes.
- Undo support for bookings and cancellations, using stacks.
- Swing GUI with buttons for booking, cancelling, seat info, expiry check and saving.
- Load seat data from a CSV file and save the current state back to CSV, so records persist between runs.

## Data structures used

| Structure | Used for |
|---|---|
| `HashMap<String, Seat>` | Constant-time seat lookup by seat number |
| AVL Tree | Keeping the seat categories balanced and ordered |
| `Stack` | Booking and cancellation history for undo |
| `HashMap` | Tracking booking times for auto-expiry |

## Seat ranges

| Category | Seats |
|---|---|
| VIP | V101 to V150 |
| Premium | P101 to P200 |
| Economy | E101 to E450 |

## Run it locally

You need the [Java Development Kit](https://adoptium.net) (JDK 11 or newer). Run these commands from the project root, because the program looks for `ticket_dataset.csv` in the current folder.

```bash
git clone https://github.com/HiraKhan13/ticket-reservation-system.git
cd ticket-reservation-system
javac -d out src/Main.java
java -cp out MainGUI
```

## How to use

1. Enter a seat number (for example `P112`) and a customer name, then click **Book Seat**.
2. Click **Cancel Seat** to cancel an existing booking.
3. **Seat Info** shows the current status of a seat.
4. **Check Expiry** removes expired bookings.
5. **Save to File** writes the current state to `ticket_data_out.csv`.

## Dataset format

`ticket_dataset.csv` has 500 seats with this header:

```
seatNumber,category,price,customerName,isBooked
V101,VIP,5000,Ali,true
```

## Author

Hira Khan, [LinkedIn](https://www.linkedin.com/in/hirakhan01)
