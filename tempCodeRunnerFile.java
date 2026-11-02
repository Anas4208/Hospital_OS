import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

class Semaphore {
    protected int value=0;

    public Semaphore() {
        value=0;
    }

    public Semaphore(int n) {
        value=n;
    }

    public synchronized void P() {
        while (value<=0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        value--;
    }

    public synchronized boolean P(long timeout) {
        long deadline= System.currentTimeMillis()+timeout;
        while (value<=0) {
            long remaining=deadline-System.currentTimeMillis();
            if ( remaining<=0) return false;
            try {
                wait(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        value--;
        return true;
    }

    public synchronized void V() {
        value++;
        notifyAll();
    }

    public synchronized int getValue() {
        return value;
    }
}

class Buffer {
    private int size;
    private Object[] store;
    private int inptr = 0;
    private int outptr = 0;
    private Semaphore spaces;
    private Semaphore elements;

    public Buffer(int capacity) {
        size = capacity;
        store = new Object[size];
        spaces = new Semaphore(size);
        elements = new Semaphore(0);
    }

    public void produce(Patient p) {
        synchronized(this) {
            if (spaces.getValue() <= 0) {
                System.out.println("The waiting room is full.");
                System.out.println("P" + p.getPatientId() + " is waiting outside for an available seat.");
            }
        }  
        spaces.P(); 
        synchronized(this) {
            store[inptr] = p;
            inptr = (inptr + 1) % size;
        }
        System.out.println("P" + p.getPatientId() + " entered the waiting room.");
        elements.V();
    }

    public Object consume() {
        Object value;
        elements.P();
        synchronized(this) {
            value = store[outptr];
            outptr = (outptr + 1) % size;
        }
        spaces.V();
        return value;
    }

    public synchronized int getSpacesValue() {
        return spaces.getValue();
    }
}

class Doctor extends Thread {
    private int id;
    private Hospital hospital;
    private volatile boolean running = true;

    public Doctor(int id, Hospital hospital) {
        this.id = id;
        this.hospital = hospital;
    }

    public int getDoctorId() {
        return id;
    }

    @Override
    public void run() {
        while (running && hospital.getCompletedPatientsCount().get() < hospital.getNumOfPatients()) {
            try {
                Object obj = hospital.getWaitingRoom().consume();
                if (obj == null)
                    break;
                Patient p = (Patient) obj;

                System.out.println("Doctor " + id + " is assigned to P" + p.getPatientId() + ".");
                System.out.println("Doctor " + id + " is trying to acquire a treatment room.");

                boolean acquired = hospital.getTreatmentRooms().P(3000);

                if (acquired) {
                    System.out.println("Doctor " + id + " acquired a treatment room.");
                    System.out.println("Doctor " + id + " begins treating P" + p.getPatientId() + ".");

                    try {
                        Thread.sleep(1000 + hospital.getRandom().nextInt(1000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } finally {
                        hospital.getTreatmentRooms().V();
                        System.out.println("Doctor " + id + " released the treatment room.");
                    }
                    System.out.println("Doctor " + id + " finished treating P" + p.getPatientId() + ".");
                    System.out.println("Doctor " + id + " is now available.");

                    System.out.println("P" + p.getPatientId() + " left.");

                    hospital.incrementCompleted();

                    if(hospital.getCompletedPatientsCount().get() == hospital.getNumOfPatients()){
                        System.out.println("All patients have been treated.");
                        System.out.println("Simulation completed.");
                        System.exit(0);
                    }
                } else {
                    System.out.println("Doctor " + id + " could not acquire a treatment room.");
                    System.out.println(
                            "Doctor " + id + " is sending P" + p.getPatientId() + " back to the waiting room.");
                    hospital.getWaitingRoom().produce(p);
                    System.out.println("P" + p.getPatientId() + " entered the waiting room.");
                }
            } catch (Exception e) {
                break;
            }
        }
    }

    public void stopDoctor() {
        running = false;
        this.interrupt();
    }
}

class Patient extends Thread {
    private int id;
    private Hospital hospital;

    public Patient(int id, Hospital hospital) {
        this.id = id;
        this.hospital = hospital;
    }

    public int getPatientId() {
        return id;
    }

    @Override
    public void run() {
        hospital.patientArrive(this);
    }
}


public class Hospital {
    private int waitingRoomCapacity;
    private int numOfDoctors;
    private int numOfPatients;

    private Buffer waitingRoom;
    private Semaphore treatmentRooms;
    private AtomicInteger completedPatients = new AtomicInteger(0);
    private Random random = new Random();

    public Hospital(int waitingRoomCapacity, int numOfDoctors, int numOfPatients) {
        this.waitingRoomCapacity = waitingRoomCapacity;
        this.numOfDoctors = numOfDoctors;
        this.numOfPatients = numOfPatients;

        waitingRoom = new Buffer(waitingRoomCapacity);
        treatmentRooms = new Semaphore(2);

        System.out.println("Patients arriving:");
        for(int i = 1; i <= numOfPatients; i++){
            System.out.print("P" + i + (i == numOfPatients ? "" : " "));
        }
        System.out.println();

        for(int i = 1; i <= numOfDoctors; i++){
            Doctor doctor = new Doctor(i, this);
            doctor.start();
        }

        for(int i = 1; i <= numOfPatients; i++){
            Patient patient = new Patient(i, this);
            patient.start();
        }
    }

    public void patientArrive(Patient p) {
        System.out.println("P" + p.getPatientId() + " arrived.");
        waitingRoom.produce(p);
        System.out.println("P" + p.getPatientId() + " entered the waiting room.");
    }

    public void incrementCompleted() {
        completedPatients.incrementAndGet();
    }

    //Getters
    public AtomicInteger getCompletedPatientsCount(){
        return completedPatients;
    }

    public int getNumOfPatients(){
        return numOfPatients;
    }

    public Buffer getWaitingRoom(){
        return waitingRoom;
    }

    public Semaphore getTreatmentRooms(){
        return treatmentRooms;
    }

    public Random getRandom(){
        return random;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter waiting room capacity: ");
        int capacity = scanner.nextInt();

        System.out.print("Enter number of doctors: ");
        int numOfDoctors = scanner.nextInt();

        System.out.print("Enter number of patients: ");
        int numOfPatients = scanner.nextInt();

        if (capacity <= 0 || numOfDoctors <= 0 || numOfPatients <= 0) {
            System.out.println("All inputs must be greater than zero.");
            return;
        }

        new Hospital(capacity, numOfDoctors, numOfPatients);

        scanner.close();
    }
}

