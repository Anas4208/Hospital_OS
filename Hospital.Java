import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

class Semaphore {
    protected int value=0;

    public Semaphore() {
    }

    public Semaphore(int initial) {
    }

    public synchronized void P() {
    }

    public synchronized boolean P(long timeout) {
        return false;
    }

    public synchronized void V() {
    }

    public synchronized int getValue() {
        return 0;
    }
}

class Buffer {
    private int size;
    private Object[] store;
    private int inptr=0;
    private int outptr=0;
    private Semaphore spaces;
    private Semaphore elements;

    public Buffer(int capacity) {
    }

    public void produce(Object value) {
    }

    public Object consume() {
        return null;
    }

    public synchronized int getSpacesValue() {
        return 0;
    }
}

class Patient extends Thread {
    private int id;
    private Hospital hospital;

    public Patient(int id, Hospital hospital) {
    }

    public int getPatientId() {
        return 0;
    }

    public void run() {
    }
}

class Doctor extends Thread {
    private int id;
    private Hospital hospital;
    private volatile boolean running=true;

    public Doctor(int id, Hospital hospital) {
    }

    public int getDoctorId() {
        return 0;
    }

    public void run() {
    }

    public void stopDoctor() {
    }
}

public class Hospital {
    private int waitingRoomCapacity;
    private int numOfDoctors;
    private int numOfPatients;

    private Buffer waitingRoom;
    private Semaphore treatmentRooms;
    private AtomicInteger completedPatients=new AtomicInteger(0);
    private Random random=new Random();

    public Hospital(int waitingRoomCapacity, int numOfDoctors, int numOfPatients) {
    }

    public void patientArrive(Patient p) {
    }

    public void doctorWork(Doctor d) {
    }

    public static void main(String[] args) {
    }
}
