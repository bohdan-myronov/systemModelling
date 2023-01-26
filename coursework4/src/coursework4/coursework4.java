package coursework4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

class Rng {

    public static double Exp(double timeMean) {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = -timeMean * Math.log(a);
        return a;
    }
}

class Entity {

    private String name;
    private double tnext;
    private double delayMean, delayDev;
    private String distribution;
    private int quantity;
    private double tcurr;
    private int state;
    public Entity nextEntity;
    private static int nextId = 0;
    private int id;
    private double averageDeviceLoad;
    int failure;
    double currentTimer;
    static double timeRecords;
    static int timerCounter;

    public Entity(double delay) {
        name = "anonymus";
        tnext = 0.0;
        delayMean = delay;
        distribution = "";
        tcurr = tnext;
        state = 0;
        nextEntity = null;
        if (nextId == 4) {
            nextId = 0;
        }
        id = nextId;
        timeRecords = 0;
        timerCounter = 0;
        nextId++;
        name = "Entity" + id;
        averageDeviceLoad = 0;
    }

    public static double round(double value, int places) {
        return value;
        /*if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
         */
    }

    public Entity(String nameOfEntity, double delay) {
        name = nameOfEntity;
        tnext = 0.0;
        delayMean = delay;
        distribution = "exp";
        tcurr = tnext;
        state = 0;
        nextEntity = null;
        id = nextId;
        nextId++;
        name = "Entity" + id;
        averageDeviceLoad = 0;
    }

    public double getDelay() {
        double delay = getDelayMean();
        if ("norm".equalsIgnoreCase(getDistribution())) {
            delay = Rng.Exp(getDelayMean() / 2) + Rng.Exp(getDelayMean() / 2);
        }
        return delay;
    }

    public void AddNewRecord(double timer) {
        timeRecords += timer;
        timerCounter++;
    }

    public int getTimerCounter() {
        return timerCounter;
    }

    public double getTimeRecords() {
        return timeRecords;
    }

    public void setCurrentTimer(double timer) {
        currentTimer = timer;
    }

    public double getCurrentTimer() {
        return currentTimer;
    }

    public double getDelayDev() {
        return delayDev;
    }

    public void setDelayDev(double delayDev) {
        this.delayDev = delayDev;
    }

    public int getFailure() {
        return failure;

    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTcurr() {
        return tcurr;
    }

    public void setTcurr(double tcurr) {
        this.tcurr = tcurr;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Entity getNextEntity() {
        return nextEntity;
    }

    public void setNextEntity(Entity nextEntity) {
        this.nextEntity = nextEntity;
    }

    public int inAct() {
        return -2;
    }

    public void outAct() {
        quantity++;
    }

    public double getTnext() {
        return tnext;
    }

    public void setTnext(double tnext) {
        this.tnext = tnext;
    }

    public double getDelayMean() {
        return delayMean;
    }

    public void setDelayMean(double delayMean) {
        this.delayMean = delayMean;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAverageDeviceLoad() {
        return averageDeviceLoad;
    }

    public void setAverageDeviceLoad(double ADL) {
        averageDeviceLoad = ADL;
    }

    public void printResult() {
        System.out.println(getName() + " quantity = " + quantity);
    }

    public void printInfo() {
        System.out.println(getName() + " state" + state
                + " processed" + quantity
                + " tnext= " + getTnext());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void doStatistics(double delta, double time) {
        double _averageDeviceLoad = getAverageDeviceLoad();
        _averageDeviceLoad += delta / time * getState();
        setAverageDeviceLoad(_averageDeviceLoad);
    }
}

class Create extends Entity {

    public Create(double delay) {
        super(delay);
        super.setTnext(0.0); // імітація розпочнеться з події Create
    }

    @Override
    public void outAct() {
        super.outAct();
        super.setTnext(super.getTcurr() + super.getDelay());
        int result = super.getNextEntity().inAct();

    }

}

class Client extends Entity {

    int[] clientsRecallCounter;
    static int counter = 0;
    double[] tnexts;
    double[] timers;
    int maxRecalls = 4;
    boolean countFailures;

    public Client(double delay, int clientBaseCapacity, boolean countFailures) {
        super(delay);
        failure = 0;
        counter = 0;
        clientsRecallCounter = new int[clientBaseCapacity];
        tnexts = new double[clientBaseCapacity];
        this.countFailures = countFailures;
        timers = new double[clientBaseCapacity];

    }

    @Override
    public int inAct() {
        tnexts[counter++] = super.getTcurr() + 0.01;
        return 0;
    }

    public int InAct(int clientId) {
        clientsRecallCounter[clientId]++;
        if (clientsRecallCounter[clientId] > 3) {
            failure++;
            System.out.println("Client " + clientId + " called 4 times and decided to use another taxi service. He spent " + timers[clientId] + " seconds. ");
            if (countFailures) {
                super.AddNewRecord(timers[clientId]);
            }
            tnexts[clientId] = Double.MAX_VALUE;
            return -1;
        } else {
            tnexts[clientId] = super.getTcurr() + 60;
            timers[clientId] += 60;
            System.out.println("Client " + clientId + " will be calling for " + clientsRecallCounter[clientId] + " time.");
        }
        return 0;
    }

    @Override
    public void outAct() {
        super.outAct();

        double tnext = getTnext();
        int clientId = getClientIndexByTnextValue(tnext);
        System.out.println("Client " + clientId + " calls.");
        tnexts[clientId] = Double.MAX_VALUE;
        CallCenter cc = (CallCenter) super.getNextEntity();
        cc.setCurrentTimer(timers[clientId]);
        cc.setCurrentClientId(clientId);
        int result = super.getNextEntity().inAct();
        if (result == -1) {
            this.InAct(clientId);
        }
    }

    int getClientsCount() {
        return counter;
    }

    public double getTnext() {
        double leastTnext = 999999999;
        for (int i = 0; i < getClientsCount(); i++) {
            if (tnexts[i] < leastTnext) {
                leastTnext = tnexts[i];
            }
        }
        return leastTnext;
    }

    public int getClientIndexByTnextValue(double tnext) {
        int id = 0;
        for (int i = 0; i < getClientsCount(); i++) {
            if (tnexts[i] == tnext) {
                id = i;
            }
        }
        return id;
    }

}

class CallCenter extends Entity {

    int[] operators;
    double[] tnexts;
    int[] clients;
    int currentClient;
    double[] timers;
    Client ClientDB;

    public CallCenter(double delay, int numberOfOperators, Client ClientDB) {
        super(delay);
        operators = new int[numberOfOperators];
        tnexts = new double[numberOfOperators];
        clients = new int[numberOfOperators];
        timers = new double[numberOfOperators];
        for (int i = 0; i < numberOfOperators; i++) {
            tnexts[i] = Double.MAX_VALUE;
        }
        this.ClientDB = ClientDB;
    }

    int getOperatorsCount() {
        return operators.length;
    }

    int getOperatorState(int index) {
        return operators[index];
    }

    void setOperatorState(int index, int value) {
        operators[index] = value;
    }

    void setOperatorTnext(int index, double tnext) {
        tnexts[index] = tnext;
    }

    @Override
    public double getTnext() {
        double leastTnext = 99999999;
        for (int i = 0; i < getOperatorsCount(); i++) {
            if (tnexts[i] < leastTnext) {
                leastTnext = tnexts[i];
            }
        }
        return leastTnext;
    }

    public int getOperatorIndexByTnextValue(double tnext) {
        int id = 0;
        for (int i = 0; i < getOperatorsCount(); i++) {
            if (tnexts[i] == tnext) {
                id = i;
            }
        }
        return id;
    }

    @Override
    public int inAct() {
        boolean requestAccepted = false;
        for (int i = 0; i < getOperatorsCount(); i++) {
            if (getOperatorState(i) == 0) {
                setOperatorState(i, 1);
                double tdelta = super.getDelay();
                setOperatorTnext(i, super.getTcurr() + tdelta);
                timers[i] = getCurrentTimer() + tdelta;
                clients[i] = currentClient;
                requestAccepted = true;
                break;
            }
        }
        if (!requestAccepted) {
            System.out.println("Operators are busy!");
            failure++;
            return -1;
        }
        return 0;
    }

    @Override
    public void outAct() {

        double leastTnext = getTnext();
        int operatorId = getOperatorIndexByTnextValue(leastTnext);
        setOperatorTnext(operatorId, Double.MAX_VALUE);
        Entity next = super.getNextEntity();
        next.setCurrentTimer(timers[operatorId]);
        if (next != null) {
            int result = next.inAct();
            if (result == -1) {
                failure++;
                ClientDB.setCurrentTimer(timers[operatorId]);
                ClientDB.InAct(clients[operatorId]);
            } else {
                super.outAct();
            }
        }
        setOperatorState(operatorId, 0);
    }

    public void setCurrentClientId(int clientId) {
        currentClient = clientId;
    }

    public int getQueue() {
        if (nextEntity instanceof Taxi) {
            Taxi p = (Taxi) nextEntity;
            return p.getQueue();
        }
        return 1 / 0;
    }

    @Override
    public void printInfo() {
        super.printInfo();
        for (int i = 0; i < operators.length; i++) {
            System.out.println("Operator  " + i + ":  state =" + operators[i] + " tnext = " + Entity.round(tnexts[i], 2));
        }
        System.out.println("failure = " + failure);
    }

    @Override
    public void doStatistics(double delta, double time) {
        double _averageDeviceLoad = getAverageDeviceLoad();
        _averageDeviceLoad += delta / time * getStatesOfTheDevices();
        setAverageDeviceLoad(_averageDeviceLoad);
    }

    int getStatesOfTheDevices() {
        int sum = 0;
        for (int i = 0; i < getOperatorsCount(); i++) {
            sum += operators[i];
        }
        return sum;
    }
}

class Taxi extends Entity {

    static float[] hits = {0.1f, 0.3f, 0.55f, 0.72f, 0.95f, 1f};
    static int[] values = {5, 8, 9, 11, 12, 20};
    int[] taxi;
    int queue = 0;
    double[] tnexts;
    float[] speed;
    boolean[] takingPassenger;
    double[] costs;
    double revenue = 0;
    double[] timers;
    Random rng = new Random();
    float baseSpeed;
    double timeDelay;
    int timeDelayRange;
    int baseSpeedRange;

    public Taxi(double delay, float baseSpeed, int baseSpeedRange, double timeDelay, int timeDelayRange, int numberOfCars, boolean rev) {
        super(delay);
        this.baseSpeed = baseSpeed;
        this.timeDelay = timeDelay;
        taxi = new int[numberOfCars];
        tnexts = new double[numberOfCars];
        takingPassenger = new boolean[numberOfCars];
        costs = new double[numberOfCars];
        timers = new double[numberOfCars];
        this.timeDelayRange = timeDelayRange;
        this.baseSpeedRange = baseSpeedRange;
        this.speed = new float[numberOfCars];
        for (int i = 0; i < numberOfCars; i++) {
            tnexts[i] = Double.MAX_VALUE;
        }
        if (rev) revenue = -1000 * numberOfCars + (-1000 * (coursework4.operators));
        
    }

    int getTaxiCount() {
        return taxi.length;
    }

    int getTaxiState(int index) {
        return taxi[index];
    }

    void setTaxiState(int index, int value) {
        taxi[index] = value;
    }

    void setTaxiTnext(int index, double tnext) {
        tnexts[index] = tnext;
    }

    @Override
    public double getTnext() {
        double leastTnext = 99999999;
        for (int i = 0; i < getTaxiCount(); i++) {
            if (tnexts[i] < leastTnext) {
                leastTnext = tnexts[i];
            }
        }
        return leastTnext;
    }

    public int getTaxiIndexByTnextValue(double tnext) {
        int id = 0;
        for (int i = 0; i < getTaxiCount(); i++) {
            if (tnexts[i] == tnext) {
                id = i;
            }
        }
        return id;
    }

    @Override
    public int inAct() {
        boolean smthChanged = false; // debug
        int distance = CalculatePath();
        for (int i = 0; i < getTaxiCount(); i++) {
            if (getTaxiState(i) == 0) {
                setTaxiState(i, 1);
                double tdelta = CalculateTimeSpentToReachClient(distance, i);
                timers[i] = getCurrentTimer() + tdelta;
                setTaxiTnext(i, super.getTcurr() + tdelta);
                smthChanged = true;
                takingPassenger[i] = true;
                System.out.println("Taxi " + i + " starts moving to the client.");
                break;
            }

        }
        //deubg
        if (!smthChanged) {
            System.out.println("error. couldn't find free taxi. ");
            return -1;
        }
        return 0;
    }

    public int inAct(int taxiId) {
        double tdelta = CalculateServiceTime();
        timers[taxiId] += tdelta;
        costs[taxiId] = 20 + tdelta * speed[taxiId] / 1000 * 3;
        setTaxiTnext(taxiId, super.getTcurr() + tdelta);
        takingPassenger[taxiId] = false;
        return 0;
    }

    @Override
    public void outAct() {

        double leastTnext = getTnext();
        int taxiId = getTaxiIndexByTnextValue(leastTnext);
        if (takingPassenger[taxiId]) {
            System.out.println("Taxi " + taxiId + " drives client to their destination.");
            this.inAct(taxiId);
        } else {
            super.outAct();
            System.out.println("Taxi " + taxiId + " completed order. Costs: " + costs[taxiId] + ". Time: " + timers[taxiId]);
            super.AddNewRecord(timers[taxiId]);
            revenue += costs[taxiId];
            setTaxiState(taxiId, 0);
            queue--;
            costs[taxiId] = 0;
            setTaxiTnext(taxiId, 9999999);
        }
    }

    public int getQueue() {
        return queue;
    }

    int CalculatePath() {
        float hit = rng.nextFloat();
        int result = 0;
        for (int i = 0; i < 6; i++) {
            if (hit < hits[i]) {
                result = values[i];
            }
        }
        return result * 1000;
    }

    double CalculateTimeSpentToReachClient(int distance, int taxiId) {
        double time;
        ChangeSpeed(taxiId);
        time = distance / speed[taxiId];
        return time;
    }

    void ChangeSpeed(int taxiId) {
        float newSpeed = (baseSpeed + rng.nextFloat(-1.0f, 1.0f) * baseSpeedRange) * 0.277778f;  // 35 +- 5 
        this.speed[taxiId] = newSpeed;
    }

    double CalculateServiceTime() {
        double serviceTime = timeDelay * 60 + rng.nextFloat(-1.0f, 1.0f) * timeDelayRange * 0.277778f;  // 40 +- 10
        return serviceTime;
    }

    @Override
    public void printInfo() {
        super.printInfo();
        for (int i = 0; i < taxi.length; i++) {
            System.out.println("Taxi " + i + ":  state =" + taxi[i] + " tnext = " + Entity.round(tnexts[i], 2));
        }
    }

    @Override
    public void doStatistics(double delta, double time) {
        double _averageDeviceLoad = getAverageDeviceLoad();
        _averageDeviceLoad += delta / time * getStatesOfTheTaxi();
        setAverageDeviceLoad(_averageDeviceLoad);
    }

    int getStatesOfTheTaxi() {
        int sum = 0;
        for (int i = 0; i < getTaxiCount(); i++) {
            sum += taxi[i];
        }
        return sum;
    }
}

class Model {

    private ArrayList<Entity> list = new ArrayList<>();
    double tnext, tcurr;
    int event;
    boolean verbose;
    static double time_;

    public Model(ArrayList<Entity> Entities, boolean verbose) {
        list = Entities;
        tnext = 0.0;
        event = 0;
        tcurr = tnext;
        this.verbose = verbose;
    }

    public void simulate(double time) {
        time_ = time;
        while (tcurr < time) {
            tnext = Double.MAX_VALUE;
            for (Entity e : list) {
                if (e.getTnext() < tnext) {
                    tnext = e.getTnext();
                    event = e.getId();
                }
            }
            System.out.println("||| " + list.get(event).getName() + ", time = " + Entity.round(tnext, 2));
            for (Entity e : list) {
                e.doStatistics(tnext - tcurr, time);
            }
            tcurr = tnext;
            for (Entity e : list) {
                e.setTcurr(tcurr);
            }
            list.get(event).outAct();
            for (Entity e : list) {
                if (e.getTnext() == tcurr) {
                    e.outAct();
                }
            }
            if (verbose) {
                printInfo();
            }
        }

        printResult();
    }

    public void printInfo() {
        for (Entity e : list) {
            e.printInfo();
        }
    }

    public void printResult() {

        int failureCounter = 0;
        int quantity;
        quantity = list.get((0)).getQuantity();
        for (Entity e : list) {
            if (verbose) {
                e.printResult();
            }
            if (e instanceof Taxi) {
                Taxi p = (Taxi) e;
                if (verbose) {
                    System.out.println("average number of cars in use:" + p.getAverageDeviceLoad());
                    System.out.println("revenue: " + p.revenue);
                }
            } else if (e instanceof CallCenter) {
                CallCenter cc = (CallCenter) e;
                if (verbose) {
                    System.out.println("average call center business:" + cc.getAverageDeviceLoad());
                }
            } else if (e instanceof Client) {
                Client cl = (Client) e;
                failureCounter += cl.failure;
            }
            if (verbose) {
                System.out.println(e.getName() + "  failure: " + e.failure);
            }

        }
        int t1 = list.get(0).getQuantity();
        int t2 = list.get(1).getFailure();
        int t3 = list.get(2).getQuantity();
        int t4 = list.get(2).getFailure();
        double t5 = list.get(0).getTimeRecords() / list.get(0).getTimerCounter();
        Taxi p = (Taxi) list.get(3);
        double t6 = p.revenue;
        double t7 = p.getAverageDeviceLoad();
        CallCenter cc = (CallCenter) list.get(2);
        double t8 = cc.getAverageDeviceLoad();
        double t9 = failureCounter / (double) quantity;
        StatsCollector.Collect(time_, t1, t2, t3, t4, t5, t6, t7, t8, t9);
        if (verbose) {
            System.out.println("records = " + list.get(0).getTimeRecords() + ",counter = " + list.get(0).getTimerCounter());
            System.out.println("average time in service = " + list.get(0).getTimeRecords() / list.get(0).getTimerCounter());
            System.out.println("overall failure probability = " + failureCounter / (double) quantity);
            System.out.println("failure " + failureCounter);
            System.out.println("overall " + quantity);
        }

    }
}

class StatsCollector {

    static double[] time = new double[1000];
    static int[] clientsCreated = new int[1000];
    static int[] clientsLost = new int[1000];
    static int[] ccIncomingCalls = new int[1000];
    static int[] ccIncomingCallsLost = new int[1000];
    static double[] timeService = new double[1000];
    static double[] revenue = new double[1000];
    static double[] avgTaxiLoad = new double[1000];
    static double[] avgOperatorLoad = new double[1000];
    static double[] failureProbability = new double[1000];
    static int counter;

    public static void Collect(double t, int cC, int cL, int ccIC, int ccICL, double tS, double r, double aTL, double aOL, double fP) {
        time[counter] = t;
        clientsCreated[counter] = cC;
        clientsLost[counter] = cL;
        ccIncomingCalls[counter] = ccIC;
        ccIncomingCallsLost[counter] = ccICL;
        timeService[counter] = tS;
        revenue[counter] = r;
        avgTaxiLoad[counter] = aTL;
        avgOperatorLoad[counter] = aOL;
        failureProbability[counter] = fP;
        counter++;
    }

    public static void print() {
        for (int i = 0; i < counter; i++) {
            System.out.println((int) time[i] + ";" + clientsCreated[i] + ";" + clientsLost[i] + ";" + ccIncomingCalls[i] + ";" + ccIncomingCallsLost[i] + ";" + Entity.round(timeService[i], 3) + ";" + Entity.round(revenue[i], 2) + ";" + Entity.round(avgTaxiLoad[i], 3) + ";" + Entity.round(avgOperatorLoad[i], 3) + ";" + Entity.round(failureProbability[i], 3));
        }
       

    
}
public static void printRev() {
    String a =  "";
        for (int i = 0; i < counter; i++) {
            a += (revenue[i]+"; ");
        }
        System.out.println(a);
    }

}
class coursework4 {

    public static int operators = 1;
    public static int taxis = 14;

    void model(int time,int operators, int taxis) {
        this.operators = operators;
        this.taxis = taxis;
        
        Create c = new Create(180);
        Client cl = new Client(0, 10000, false);
        CallCenter p1 = new CallCenter(30, operators, cl);
        Taxi p2 = new Taxi(0, 35, 5, 40, 10, taxis,true);// 0 не використовується

        c.setNextEntity(cl);
        cl.setNextEntity(p1);
        p1.setNextEntity(p2);

        c.setName("Creator");
        cl.setName("Client");
        p1.setName("Call Center");
        p2.setName("Taxi");

        c.setDistribution("norm");

        ArrayList<Entity> list = new ArrayList<>();
        list.add(c);
        list.add(cl);
        list.add(p1);
        list.add(p2);

        Model model = new Model(list, false);
        model.simulate(time); // 24 h
    }

    public static void main(String[] args) {
        int time = 86400;
        int ops = 1;
        int taxi = 14;
        coursework4 a = new coursework4();
        for (int i = 1; i != 0; i--) {
            //time = 86400/i;
            //ops = 5;
            for (int j = 1; j <= 14; j++){
                taxi = j;
                ops = 15-j;
                a.model(86400,ops,taxi);
            }
            
            
        }
        StatsCollector.print();
        //StatsCollector.printRev();
    }
}
