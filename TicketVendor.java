package ticketVendor;

public class TicketVendor {
    private int seatsRemaining, cashOnHand, revenue, STARTING_CASH, TOTAL_SEATS;
    private static final int PRICE = 1000;

    public TicketVendor(int startingCash, int totalSeats) {
        this.STARTING_CASH = startingCash;
        this.TOTAL_SEATS = totalSeats;
        this.seatsRemaining = TOTAL_SEATS;
        this.cashOnHand = STARTING_CASH;
        this.revenue = 0;
    }

    public void sellTicket(int syncOrUn){
        System.out.println(Thread.currentThread().getName() + ": Seats remaining: " + seatsRemaining);
        if(seatsRemaining > 0){
            dispenseTicket();
            if(syncOrUn == 0) {
                // synchronized version
                synchronized (this) {
                    seatsRemaining = seatsRemaining - 1;
                    cashOnHand = cashOnHand + PRICE;
                }
            } else {
                // non-synchronized version with race condition
                try {
                    Thread.sleep(100); // introduce delay to allow race condition
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                seatsRemaining = seatsRemaining - 1;
                cashOnHand = cashOnHand + PRICE;
            }
        } else {
            displaySorrySoldOut();
        }
    }
    
    public void audit(int syncOrUn) {
        synchronized (this) { // Synchronize access to ensure consistency
            revenue = (TOTAL_SEATS - seatsRemaining) * PRICE;
            if (cashOnHand != revenue + STARTING_CASH) {
                System.out.println("Cash fails to match.");
            }
            System.out.println("Revenue: " + revenue + ", Cash on hand: " + cashOnHand);
        }
    }

    private void dispenseTicket() {
        System.out.println(Thread.currentThread().getName() + ": Printing ticket...");
        System.out.println("{:One ticket:}");
    }

    private void displaySorrySoldOut() {
        System.out.println(Thread.currentThread().getName() + ": Sorry, Sold Out :(");
    }


    private class SaleRunnable implements Runnable {
        public void run() {
            sellTicket(0);
            audit(0);
        }
    }
    
    private class SaleRunnable2 implements Runnable {
        public void run() {
        	sellTicket(1);
            audit(1);
        }
    }

    private static void concurrent(TicketVendor vendor) throws InterruptedException {
        Thread sale1 = new Thread(vendor.new SaleRunnable(), "Thread 1");
        Thread sale2 = new Thread(vendor.new SaleRunnable(), "Thread 2");

        sale1.start();
        Thread.sleep(10); // Sleep to introduce a race condition
        sale2.start();
        sale1.join();
        sale2.join();
    }
    
    private static void concurrent2(TicketVendor vendor2) throws InterruptedException {
        Thread sale1 = new Thread(vendor2.new SaleRunnable2(), "Thread 1");
        Thread sale2 = new Thread(vendor2.new SaleRunnable2(), "Thread 2");

        sale1.start();
        Thread.sleep(10); // Sleep to introduce a race condition
        sale2.start();
        sale1.join();
        sale2.join();
    }

    public static void main(String[] args) throws InterruptedException {
    	System.out.println("Synchronized:");
        TicketVendor vendor = new TicketVendor(0, 1);
        concurrent(vendor);
    	System.out.println("\nUnsynchronized:");
        TicketVendor vendor2 = new TicketVendor(0, 1);
        concurrent2(vendor2);
    }
}
