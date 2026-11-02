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
        value--;
        if(value<0){
            try {wait();} catch (InterruptedException e) {}
        }
    }

    public synchronized boolean P(long timeout) {
        value--;
        if (value<0){
            long beginTime=System.currentTimeMillis();
            long passedTime=0;
            try{
                while(value<0){
                    long remainingTime=timeout-passedTime;
                    if(remainingTime<=0){
                        value++;
                        return false;
                    }
                    wait(remainingTime);
                    passedTime=System.currentTimeMillis()-beginTime;

                }
            }
            catch(InterruptedException e) {
                value++;
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    public synchronized void V() {
        value++;
        if (value<=0) notify();
    }

    public synchronized int getValue() {
        return value;
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
        size = capacity;
        store = new Object[size];
        spaces = new Semaphore(size);
        elements = new Semaphore(0);
    }

    public void produce(Object value) {
        spaces.P();
        store[inptr] = value;
        inptr = (inptr + 1) % size;
        elements.V();
    }

    public Object consume() {
        Object value;
        elements.P();
        value = store[outptr];
        outptr = (outptr + 1) % size;
        spaces.V();
        return value;
    }

    public synchronized int getSpacesValue() {
        return spaces.getValue();
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
class Doctor extends Thread{
    private int id;
    private Hospital hospital;
    private volatile boolean running = true;

    public Doctor(int id, Hospital hospital){
        this.id=id;
        this.hospital=hospital;
    }

    public int getDoctorId(){
        return id;
    }

    @Override
    public void run(){
        while (running && hospital.getCompletedPatientsCount() < hospital.getNumOfPatients()) {
            try{
                Object obj = hospital.getWaitingRoom().consume();
                if(obj==null) break;
                Patient p = (Patient) obj;

                System.out.println("Doctor " + id + " is assigned to P" + p.getPatientId() + ".");
                System.out.println("Doctor " + id + " is trying to acquire a treatment room.");

                boolean acquired = hospital.getTreatmentRooms().P(3000);

                if(acquired){
                    System.out.println("Doctor " + id + " acquired a treatment room.");
                    System.out.println("Doctor " + id + " begins treating P" + p.getPatientId() + ".");
                    

                    try{
                        Thread.sleep(1000 + hospital.getRandom().nextInt(1000));
                    }catch(InterruptedException e){
                        break;
                    }
                    System.out.println("Doctor " + id + " finished treating P" + p.getPatientId() + ".");

                    hospital.getTreatmentRooms().V();
                    System.out.println("Doctor " + id + " released the treatment room.");
                    System.out.println("Doctor " + id + " is now available.");

                    hospital.incrementCompleted();
                }else{
                    System.out.println("Doctor " + id + " could not acquire a treatment room.");
                    System.out.println("Doctor " + id + " is sending P" + p.getPatientId() + " back to the waiting room.");

                    synchronized ( hospital.getWaitingRoom()){
                        if (hospital.getWaitingRoom().getSpacesValue() <= 0) {
                            System.out.println("The waiting room is full.");
                            System.out.println("P" + p.getPatientId() + " is waiting outside for an available seat.");
                        }
                    }
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
    
    class Patient extends Thread{
        private int id;
        private Hospital hospital;

        public Patient(int id, Hospital hospital){
            this.id = id;
            this.hospital = hospital;
        }
        public int getPatientId(){
            return id;
        }

        @Override
        public void run(){
            hospital.patientArrive(this);
        }
    }

