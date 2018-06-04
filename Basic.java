import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.io.ByteArrayOutputStream;
import javax.imageio.*;
import java.io.*;

public class Basic implements ActionListener
{
	private Board field;
	private JFrame wnd;
	private JPanel buttons;
	private JLabel counterLabel;
	private JButton start, stop, next, save;
	private javax.swing.Timer timer;
	private ActionListener timerTick;
	private int counter;
	
	private PrintWriter file;
	
	Basic()
	{
		wnd = new JFrame("Simulator");
		
		counter = 0;
		
		counterLabel = new JLabel("0");
		
		buttons = new JPanel(null);
		Insets insets = buttons.getInsets();
		//System.out.println(insets);
		start = new JButton("Start");
		stop = new JButton("Stop");
		next = new JButton("Krok");
		save = new JButton("Zapisz");
		start.addActionListener(this);
		stop.addActionListener(this);
		next.addActionListener(this);
		save.addActionListener(this);
		
		Dimension size = counterLabel.getPreferredSize();
		// System.out.println(size);
		counterLabel.setBounds(10 + insets.left, 10 + insets.top, 80, size.height);
		size = start.getPreferredSize();
		start.setBounds(10 + insets.left, 40 + insets.top, size.width, size.height);
		size = stop.getPreferredSize();
		stop.setBounds(10 + insets.left, 70 + insets.top, size.width, size.height);
		size = next.getPreferredSize();
		next.setBounds(10 + insets.left, 100 + insets.top, size.width, size.height);
		size = save.getPreferredSize();
		save.setBounds(10 + insets.left, 130 + insets.top, size.width, size.height);
		
		buttons.setPreferredSize(new Dimension(100, 300));
		buttons.add(counterLabel);
		buttons.add(start);
		buttons.add(stop);
		buttons.add(next);
		buttons.add(save);
		//System.out.println(buttons.getPreferredSize());
		
		//ca = new Rule184(10000, 0.15, 0.1);
		//field = new Board(ca, 500, 50);
		
		try {
			this.file  = new PrintWriter(new File("plik.csv"));
		} catch (FileNotFoundException fnfe)
		{
			System.out.println("Nie ma takiego pliku");
		}
		
		field = new Board(new MultiLane(2, 10000, 0.1, 0.1, 0.5, 0.5, file), 500, 50);
		// field = new Board(new MultiLane(1, 1000, 0.2, 0, file), 500, 50);
		
		
		/* for (int i = 0; i < 320; i++)
			field.step();
		field.repaint(); */
		
		
		int delay = 30;
		timerTick = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				field.repaint();
				field.step();
				counter++;
				counterLabel.setText(Integer.toString(counter));
				/* if (counter % 10000 == 0)
				{	
					counterLabel.setText(Integer.toString(counter));
					timer.stop();
				} */
			}
		};
		timer = new javax.swing.Timer(delay, timerTick);
		//timer.start();
		
		wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		wnd.setLayout(new BorderLayout());
		wnd.add(field, BorderLayout.CENTER);
		wnd.add(buttons, BorderLayout.EAST);
		
		wnd.validate();
		wnd.setSize(700, 700);
		wnd.setExtendedState(wnd.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		wnd.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		switch (ae.getActionCommand())
		{
			case "Start":
				timer.start();
				break;
			case "Stop":
				timer.stop();
				break;
			case "Krok":
				timerTick.actionPerformed(new ActionEvent(this, 0, null));
				break;
			case "Zapisz":
				//this.field.mca.save();
				break;
			default:
				System.out.println("Coś się popsuło");
		}
	}
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable() { public void run() { new Basic(); }});
		
		/* PrintWriter plik = null;
		
		try {
			plik = new PrintWriter(new File("Model4_ver4_v3_d53_pc004.csv"));
		} catch (FileNotFoundException fnfe)
		{
			System.out.println("Nie ma takiego pliku");
		}
		
		MultiLane m;
		for (int i = 2; i <= 90; i += 2)
		{
			m = new MultiLane(2, 10000, i/100.0, 0.1, 0.5, 0.04, plik);
			System.out.print(i + "... ");
			
			int j = 0;
			for (; j < 1000; j++)
				m.step();
			
			int nRazy = 6;
			//System.out.print("halfway... ");
			for (; j < 1000 + nRazy*300; j++)
				m.step();
			
			//m.save(plik);
			System.out.println("done");
		}
		plik.close(); */
		
	}
}

class Board extends JPanel
{
	private javax.swing.Timer timer;
	
	private LinkedList<byte[]>[] plot;
	private int plotHeight = 301;
	private int plotWidth = 400;
	private int plotXOffset = 1;
	private int origX = 20, origY = 20;
	private int cycles;
	
	private int cellWidth, cellHeight, carSpacing;
	public MultiLane mca;
	private byte[][] currLanes;
	
	Board(MultiLane automaton, int width, int offset)
	{
		
		setLayout(null);
		plot = new LinkedList[automaton.numOfLanes()];
		for (int i = 0; i < automaton.numOfLanes(); i++)
			plot[i] = new LinkedList<>();
		
		mca = automaton;
		plotWidth = (mca.length() < width) ? mca.length() : width;
		int max = 600/automaton.numOfLanes();
		plotWidth = (plotWidth > max) ? max : plotWidth;
		
		cycles = 0;
		plotXOffset = offset;
		cellHeight = 5;
		cellWidth = 10;
		carSpacing = 2;
		
		
		for (int i = 0; i < mca.numOfLanes(); i++)
			for (int j = 0; j < plotHeight; j++)
				plot[i].add(new byte[plotWidth]);
		
		currLanes = mca.visual(plotXOffset, plotXOffset+plotWidth);
		for (int i = 0; i < mca.numOfLanes(); i++)
		{
			plot[i].addLast(currLanes[i].clone());
			plot[i].removeFirst();
		}
		
		repaint();
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		draw(g);
	}
	
	public void step()
	{
		mca.step();
		
		currLanes = mca.visual(plotXOffset, plotXOffset+plotWidth);
		
		for (int i = 0; i < mca.numOfLanes(); i++)
		{
			plot[i].addLast(currLanes[i].clone());
			plot[i].removeFirst();
		}
	}
	
	
	/* public void step()
	{
		ca.step();
		
		byte[] tmp = new byte[plotWidth];
		
		// int pattern;
		// for (int i = plotXOffset; i < plotWidth; i++)
		// {
			// pattern = 4*cur[i-1] + 2*cur[i] + cur[i+1];
			// if (pattern == 1 ||pattern == 3 ||pattern == 4 || pattern == 6)
				// nxt[i] = 1;
			// else
				// nxt[i] = 0 ;
		// }
		// byte[] t = cur;
		// cur = nxt;
		// nxt = t;
		// for (int i = plotXOffset; i < plotWidth; i++)
			// nxt[i] = 0; 
		
		// for (int i = 0; i < plotWidth; i++)
		// {
			// if (cur[i+plotXOffset] == 1)
				// tmp[i] = 1;
		// }
		
		
		for (int i = 0; i < plotWidth; i++)
		{
			if (ca.lane()[i+plotXOffset] != null)
				tmp[i] = 1;
		}
		
		plot.addLast(tmp);
		plot.removeFirst();
	} */
	
	private void draw(Graphics g)
	{
		//Graphics2D g2d = (Graphics2D) g.create();
		setBackground(Color.white);
		g.setColor(Color.black);
		
		for (int i = 0; i < mca.numOfLanes(); i++)
		{
			for (int j = 0; j < plotHeight; j++)
			{
				for (int k = 0; k < plotWidth; k++)
					if (plot[i].get(j)[k] == 1)
						g.fillRect(origX + i*2*(plotWidth + 8) + 2*k, origY + 2*j, 2, 2);
			}
		}
		
		int Y = origY + 2*plotHeight + 10;
		
		for (int i = 0; i < mca.numOfLanes(); i++)
		{
			for (int j = 0; j*cellWidth < 1200-origX; j++)
			{
				if (currLanes[i][j] == 1)
					g.fillRect(origX + cellWidth*j, Y + cellHeight*i, cellWidth-carSpacing, cellHeight);
				if (currLanes[i][j] == 2)
				{
					g.setColor(Color.red);
					g.fillRect(origX + cellWidth*j, Y + cellHeight*i, cellWidth, cellHeight);
					g.setColor(Color.black);
				}
				if (currLanes[i][j] == 3)
				{
					g.setColor(Color.green);
					g.fillRect(origX + cellWidth*j, Y + cellHeight*i, cellWidth, cellHeight);
					g.setColor(Color.black);
				}
			}
		}
		
	}
}

class RoadObject
{
	public static final int Vehicle = 1;
	public static final int Roadblock = 2;
	private int type;
	
	RoadObject(int type)
	{
		this.type = type;
	}
	
	public int type()
	{
		return type;
	}
}

enum VehicleType {Car, Truck;}

class Vehicle extends RoadObject
{
	private static int IDCount = 1;
	private static int[] gfmin = {0, 0, 0, 2, 2, 4};
	private static int[][] gbmin = {	{0, 1, 4, 6, 8, 10},
										{0, 1, 2, 6, 8, 10},
										{0, 1, 2, 3, 8, 10},
										{0, 1, 2, 3, 4, 10},
										{0, 1, 2, 3, 4, 5},
										{0, 1, 2, 3, 4, 5} };
	
	enum Lane { Left, Current, Right; }
	
	private Road road;
	private VehicleType type;
	private int privilegedLane;
	private boolean isPrivilegedLane;
	
	
	private boolean slowStart;
	private int id;
	private int length;
	private int maxSpeed;
	private int speed;
	private int lane;
	private int position;
	private double pFault, pSlow, pChange;
	private int defaultMaxSpeed;
	private int defaultLookBackward;
	
	private int lookForward, lookBackward;
	
	private static Random gen = new Random();
	
	Vehicle(Road road, int lane, int position, double probFault, double probSlow, double probChange)
	{
		super(RoadObject.Vehicle);
		
		this.road = road;
		this.position = position;
		this.lane = lane;
		this.isPrivilegedLane = false;
		
		this.id = IDCount++;
		this.length = 1;
		this.speed = 0;
		this.maxSpeed = 5;
		this.pFault = probFault;
		this.pSlow = probSlow;
		this.pChange = probChange;
		this.slowStart = false;
		
		this.defaultMaxSpeed = this.maxSpeed;
		this.defaultLookBackward = 2*this.defaultMaxSpeed;
		
		lookForward = 2*maxSpeed;
		lookBackward = 2*maxSpeed;
	}
	
	private int gapFront(int lane)
	{
		int i;
		RoadObject t;
		
		if (road.openBoundaries())
			for (i = 1; i <= lookForward; i++)
			{
				if (position + i >= road.length())
				{
					i =  lookForward + 1;
					break;
				}
				
				if (road.lanes()[lane][position+i] != null)
					break;
			}
		else	// periodic
			for (i = 1; i <= lookForward; i++)
				if (road.lanes()[lane][(position+i) % road.length()] != null)
					break;
		
		return i - 1;
	}
	
	private int gapBack(int lane)
	{
		int i = (lane == this.lane) ? 1 : 0;
		int localLookBack = lookBackward + length - 1;
		RoadObject ro;
		
		if (road.openBoundaries())
			for ( ; i <= localLookBack; i++)
			{
				if (position - i < 0)
				{
					i = localLookBack + 1;
					break;
				}
				
				if ((ro = road.lanes()[lane][position - i]) != null)
				{
					if (ro.type() == RoadObject.Roadblock)
						i = localLookBack + 1;
					break;
				}
			}
		else	// periodic
			for ( ; i <= localLookBack; i++)
				if ((ro = road.lanes()[lane][(road.length() + position - i) % road.length()]) != null)
				{	
					if (ro.type() == RoadObject.Roadblock && i > 0)
						i = localLookBack + length;
					break;
				}
				
		return i - length;
	}
	
	public void changeLaneIfNeeded()
	{	
		switch (whichLaneNext_CS())
		{
			case Left:
				if (gen.nextDouble() < (1.0 - pChange))
					return;
				this.lane--;
				break;
			case Right:
				if (gen.nextDouble() < (1.0 - pChange))
					return;
				this.lane++;
				break;
		}
	}
	
	private Lane whichLaneNext()
	{
		int gf, gfl, gfr, gbl, gbr;
		Lane ret = Lane.Current;
		
		gf = gapFront(this.lane);
		gfl = (this.lane > 0) ? gapFront(this.lane - 1) : 0;
		gfr = (this.lane < this.road.numOfLanes() - 1) ? gapFront(this.lane + 1) : 0;
		gbl = (this.lane > 0) ? gapBack(this.lane - 1) : 0;
		gbr = (this.lane < this.road.numOfLanes() - 1) ? gapBack(this.lane + 1) : 0;
		
		if (this.isPrivilegedLane)
		{
			if (this.privilegedLane == this.lane)
				return ret;	// to samo co return Lane.Current
			else if (this.privilegedLane > this.lane)	// sprobuj zmienic pas na prawy
			{
				if (gbr == lookBackward)	// #1 bez drugiego warunku
					ret = Lane.Right;
			}
			else	// sprobuj zmienic pas na lewy
			{
				if (gbl == lookBackward)
					ret = Lane.Left;
			}
		}
		else if	(gf != maxSpeed && gf < speed + 1)	// roważ czy na innym pasie jest lepiej
		{	
			// jeśli lewy pas jest lepszy od środkowego
			if (gbl == lookBackward && gfl > gf)
			{
				// jeśli prawy pas jest tak samo dobry jak lewy
				if (gbr == lookBackward && gfr > gf)
					ret = (gen.nextDouble() < 0.5) ? Lane.Left : Lane.Right;
				else
					ret = Lane.Left;
			}	// jesli prawy pas jest lepszy od środkowego i lepszy od lewego
			else if (gbr == lookBackward && gfr > gf)
				ret = Lane.Right;
			// nie potrzeba else, bo ret jest równe Lane.Current
		}
		
		return ret;
	}
	
	private Lane whichLaneNext_CS()
	{
		int gf = 0, gfl = 0, gfr = 0, gbl = 0, gbr = 0;
		Lane ret = Lane.Current;
		Vehicle prevLeft = null, prevRight = null;
		
		int pos;
		
		if (this.lane > 0) // jest pas po lewej
		{
			for (pos = 0; true; pos++)
			if (this.road.lanes()[this.lane - 1][(road.length() + this.position - pos) % road.length()] != null)
				break;
			if (this.road.lanes()[this.lane - 1][(road.length() + this.position - pos) % road.length()].type() == RoadObject.Vehicle)
				prevLeft = (Vehicle)this.road.lanes()[this.lane - 1][(road.length() + this.position - pos) % road.length()];
			
			gfl = gapFront(this.lane - 1);
			gbl = gapBack(this.lane - 1);
		}
		
		if (this.lane < this.road.numOfLanes() - 1) // jest pas po prawej
		{
			for (pos = 0; true; pos++)
			if (this.road.lanes()[this.lane + 1][(road.length() + this.position - pos) % road.length()] != null)
				break;
			if (this.road.lanes()[this.lane + 1][(road.length() + this.position - pos) % road.length()].type() == RoadObject.Vehicle)
				prevRight = (Vehicle)this.road.lanes()[this.lane + 1][(road.length() + this.position - pos) % road.length()];
			
			gfr = gapFront(this.lane + 1);
			gbr = gapBack(this.lane + 1);
		}
		
		gf = gapFront(this.lane);
		
		if (this.isPrivilegedLane)
		{
			if (this.privilegedLane == this.lane)
				return ret;	// to samo co return Lane.Current
			else if (this.privilegedLane > this.lane)	// sprobuj zmienic pas na prawy
			{
				if (gbr >= gbmin[speed][prevRight.speed()] && (gfr >= gfmin[speed] || gfr >= gf))	// #1 bez drugiego warunku
					ret = Lane.Right;
			}
			else	// sprobuj zmienic pas na lewy
			{
				if (gbl >= gbmin[speed][prevLeft.speed()] && (gfl >= gfmin[speed] || gfl >= gf))
					ret = Lane.Left;
			}
		}
		else if	(gf != maxSpeed && gf < speed + 1)	// roważ czy na innym pasie jest lepiej
		{	
			// jeśli lewy pas jest lepszy od środkowego
			if ((gbl == lookBackward || (prevLeft != null && gbl >= gbmin[speed][prevLeft.speed()])) && gfl > gf)
			{
				// jeśli prawy pas jest tak samo dobry jak lewy
				if ((gbr == lookBackward || (prevRight != null && gbr >= gbmin[speed][prevRight.speed()])) && gfr > gf)
					ret = (gen.nextDouble() < 0.5) ? Lane.Left : Lane.Right;
				else
					ret = Lane.Left;
			}	// jesli prawy pas jest lepszy od środkowego i lepszy od lewego
			else if ((gbr == lookBackward || (prevRight != null && gbr >= gbmin[speed][prevRight.speed()])) && gfr > gf)
				ret = Lane.Right;
			// nie potrzeba else, bo ret jest równe Lane.Current
		}
		
		return ret;
	}
	
	public void CS_model()
	{
		int gf = gapFront(this.lane);
		Vehicle next = null;
		int s = speed;
		
		// aby przyhamował mijając znak ograniczenia prędkości
		if (speed > maxSpeed)
			speed = maxSpeed;
		
		int pos;
		for (pos = 1; true; pos++)
			if (this.road.lanes()[this.lane][(this.position + pos) % road.length()] != null)
				break;
		if (this.road.lanes()[this.lane][(this.position + pos) % road.length()].type() == RoadObject.Vehicle)
			next = (Vehicle)this.road.lanes()[this.lane][(this.position + pos) % road.length()];
		
		// SLOW TO START
		if (slowStart && gf > 0)
		{
			if (gen.nextDouble() < (1 - pSlow))
				speed++;
			slowStart = false;
			// przejdz na koniec funkcji
		}
		
		// DECCELERATION
		if (gf < speed)
		{
			if ((next != null && speed < next.speed()) || speed <= 2)
				speed = gf;
			else
				speed = Math.min(gf, speed-2);
		}
		else if (gf < 2*speed) // gf < 2*speed ale to trzeba zmienic lookFront
		{
			if ((next == null && speed >= 4) || (next != null && speed >= next.speed() + 4))
				speed -= 2;
			else if ((next == null && speed >= 2) || (next != null && speed >= next.speed() + 2))
				speed--;
			// ACCELERATION
			else if (speed < maxSpeed && speed + 1 <= gf)
				speed++;
		}
		else if (speed < maxSpeed && speed + 1 <= gf)
			speed++;
			
		
		// RANDOMIZATION
		if (speed > 0 && gen.nextDouble() < pFault)
			speed--;
		
		if (s > 0 && speed == 0)
			slowStart = true;
		
		position = (road.openBoundaries()) ? (position + speed) : ((position+speed) % road.length());
	}
	
	public void transitionFunction()
	{	
		int gf = gapFront(this.lane);
		int s = speed;
		
		// BJH
		if (slowStart && gf > 0)
		{
			if (gen.nextDouble() < (1 - pSlow))
				speed++;
			slowStart = false;
		}
		else
		{
			speed++;
			if (speed > maxSpeed)
				speed = maxSpeed;
		}
		
		// NaSch
		/* speed++;
		if (speed > maxSpeed)
			speed = maxSpeed; */
		
		if (gf < speed)
			speed = gf;
		
		// po zmianie pasa aktualizuj
		// this.isLeftLane = (this.lane < this.road.numOfLanes() - 1) ? true : false;
		// this.isRightLane = (this.lane > 0) ? true : false;
		
		if (speed > 0 && gen.nextDouble() < pFault)
			speed--;
		
		// BJH
		if (s > 0 && speed == 0)
			slowStart = true;
		
		position = (road.openBoundaries()) ? (position + speed) : ((position+speed) % road.length());
	}
	
	/* public void passingSign(Sign sign)
	{
		if (this.lane != sign.lane())
			return;
		
		if ((this.position - this.speed) < sign.pos() && this.position >= sign.pos())
			switch (sign.type())
			{
				case Sign.SpeedSign:
					lookForward = lookBackward = maxSpeed = ((SpeedSign)sign).speed();
					break;
				case Sign.MergeLaneSign:
					isPrivilegedLane = ((MergeLaneSign)sign).isPrivileged();
					privilegedLane = ((MergeLaneSign)sign).privilegedLane();
					break;
			}
	} */
	
	public void mergeLaneSign(MergeRange mrs)
	{
		isPrivilegedLane = true;
		privilegedLane = mrs.privilegedLane();
	}
	
	public void mergeLaneSign()
	{
		isPrivilegedLane = false;
	}
	
	public void speedRangeSign(SpeedRange srs)
	{
		lookForward = maxSpeed = srs.maxSpeed(this.position);
		lookBackward = srs.lookBack(this.position);
	}
	
	public void speedRangeSign()
	{
		lookForward = maxSpeed = this.defaultMaxSpeed;
		lookBackward = this.defaultLookBackward;
	}
	
	public int speed()
	{
		return speed;
	}
	
	public int lane()
	{
		return lane;
	}
	
	public int pos()
	{
		return position;
	}
	
	public int id()
	{
		return this.id;
	}
}

class trafficData
{
	private int lane, position, cycles;
	private int flowCount, densityCount;
	private double speedCount;
	private int[] speedDistribution;
	
	trafficData(int lane, int position, int flowCount, double speedCount, int densityCount, int cycles, int[] tab)
	{
		this.lane = lane;
		this.position = position;
		this.cycles = cycles;
		this.flowCount = flowCount;
		this.speedCount = speedCount;
		this.densityCount = densityCount;
		this.speedDistribution = tab.clone();
		
	}
	
	public int numOfCycles()
	{
		return this.cycles;
	}
	
	public int flowCount()
	{
		return this.flowCount;
	}
	
	public int densityCount()
	{
		return this.densityCount;
	}
	
	public double harmonicSpeedSum()
	{
		return this.speedCount;
	}
	
	public int lane()
	{
		return this.lane;
	}
	
	public int pos()
	{
		return this.position;
	}
	
	public double flow()
	{
		return (double)flowCount/cycles;
	}
	
	public double speed()
	{
		return (speedCount == 0) ? 0 : flowCount/speedCount;
	}
	
	public double density()
	{
		return (double)densityCount/cycles;
	}
	
	public int[] histogram()
	{
		return speedDistribution;
	}
}

class SpeedRange
{
	int beg, end, speedLimitBefore, speedLimit, speedLimitAfter;
	int ibeg, iend;
	
	SpeedRange(int beg, int end, int speedLimitBefore, int speedLimit, int speedLimitAfter)
	{
		this.beg = beg;
		this.ibeg = beg + 2*speedLimitBefore;
		this.iend = end;
		this.end = end + 2*speedLimit;
		this.speedLimitBefore = speedLimitBefore;
		this.speedLimit = speedLimit;
		this.speedLimitAfter = speedLimitAfter;
	}
	
	public boolean inRange(Vehicle v)
	{
		return (v.pos() >= this.beg && v.pos() < this.end) ? true : false;
	}
	
	public int beg()
	{
		return this.beg;
	}
	
	public int end()
	{
		return this.end;
	}
	
	public int maxSpeed(int pos)
	{
		return (pos <= this.iend) ? this.speedLimit : speedLimitAfter;
	}
	
	public int lookBack(int pos)
	{
		/* if (pos < this.ibeg)
			return speedLimitBefore;
		else
			return speedLimit; */
		// model CS
		if (pos < this.ibeg)
			return 2 * speedLimitBefore;
		else
			return 2 * speedLimit;
	}
}

class MergeRange
{
	int beg, end, whichLane;
	
	MergeRange(int beg, int end, int whichLanePrivileged)
	{
		this.beg = beg;
		this.end = end;
		this.whichLane = whichLanePrivileged;
	}
	
	public boolean inRange(Vehicle v)
	{
		return (v.pos() >= this.beg && v.pos() < this.end) ? true : false;
	}
	
	public int beg()
	{
		return this.beg;
	}
	
	public int end()
	{
		return this.end;
	}
	
	public int privilegedLane()
	{
		return this.whichLane;
	}
}

class carCounter
{
	int beg, end, size;
	int[][] time;
	
	class pair
	{
		public int step;
		public int lane;
		
		pair(int s, int l)
		{
			step = s;
			lane = l;
		}
	}
	HashMap<Integer, pair> map;
	
	carCounter(int beg, int end, int timeTableSize)
	{
		this.beg = beg;
		this.end = end;
		this.size = timeTableSize;
		this.time = new int[2][timeTableSize+2];
		map = new HashMap<>();
	}
	
	public void passingVehicle(Vehicle veh, int step)
	{	
		if ((veh.pos() - veh.speed()) < beg && veh.pos() >= beg)
		{
			// dodaj do listy
			map.put(veh.id(), new pair(step, veh.lane()));
		}
		
		if ((veh.pos() - veh.speed()) < end && veh.pos() >= end)
		{
			// usun z listy i dodaj do statystyk
			pair p;
			if ((p = map.get(veh.id())) != null)
			{
				int indx = (step - p.step > size) ? size+1 : step - p.step;
				time[p.lane][indx]++;
				map.remove(veh.id());
			}
		}
	}
	
	public void clear()
	{
		map.clear();
		time = new int[2][this.size+2];
	}
	
	public int[][] data()
	{	
		return time;
	}
}

class LoopDetector
{
	private int lane, position, cycles;
	private int fCount, dCount;
	private double sCount;
	private int[] speedHistogram;
	
	LoopDetector(int lane, int position, int numOfCycles)
	{
		this.lane = lane;
		this.position = position;
		this.cycles = numOfCycles;
		fCount = dCount = 0;
		sCount = 0;
		speedHistogram = new int[5];
	}
	
	public void passingVehicle(Vehicle veh)
	{
		if (this.lane != veh.lane())
			return;
		
		if (veh.pos() == this.position)
			dCount++;
		
		if ((veh.pos() - veh.speed()) < this.position && veh.pos() >= this.position)
		{
			fCount++;
			sCount += 1.0/veh.speed();
			speedHistogram[veh.speed()-1]++;
		}
	}
	
	public int lane()
	{
		return this.lane;
	}
	
	public int pos()
	{
		return this.position;
	}
	
	public trafficData data()
	{
		trafficData tmp =  new trafficData(this.lane, this.position, fCount, sCount, dCount, cycles, speedHistogram);
		fCount = 0;
		dCount = 0;
		sCount = 0;
		for (int i = 0; i < 5; i++)
			speedHistogram[i] = 0;
		return tmp;
	}
}

class Road
{
	private RoadObject[][] currLanes, nextLanes;
	//private Sign[] signs;
	private LoopDetector[] loopDetectors;
	private carCounter[] carCounters;
	private MergeRange[] mergeSigns;
	private SpeedRange[] speedSigns;
	
	private int measureCount;
	private int numOfLanes;
	private int length;	// in cells
	private boolean open;
	
	private int dataAquisitionCycle;
	
	Road(int numOfLanes, int length, boolean openBoundaries, MergeRange[] mergeSigns, SpeedRange[] speedSigns, LoopDetector[] detectors, int dataCycles, carCounter[] counters)
	{
		this.measureCount = 0;
		this.numOfLanes = numOfLanes;
		this.length = length;
		//this.signs = signs;
		this.loopDetectors = detectors;
		this.carCounters = counters;
		this.dataAquisitionCycle = dataCycles;
		
		this.mergeSigns = mergeSigns;
		
		this.speedSigns = speedSigns;
		
		currLanes = new RoadObject[numOfLanes][length];
		nextLanes = new RoadObject[numOfLanes][length];
		
		open = openBoundaries;
	}
	
	public void subStep()
	{
		Vehicle tmp;
		
		for (int i = 0; i < numOfLanes; i++)
			for (int j = 0; j < length; j++)
				if (currLanes[i][j] != null)
				{
					switch (currLanes[i][j].type())
					{
						case RoadObject.Vehicle:
							tmp = (Vehicle)currLanes[i][j];
							
							boolean f = true;
							for (MergeRange mr : mergeSigns)
							{
								if (mr.inRange(tmp))
								{
									tmp.mergeLaneSign(mr);
									f = false;
									break;
								}
							}
							if (f)
								tmp.mergeLaneSign();
							
							tmp.changeLaneIfNeeded();
							
							// jesli warunki brzegowe sa pochlaniajace to position moze byc > length,
							// w takim wypadku samochod nie pojawia sie w kolejnym kroku czasowym
							if (tmp.pos() > length)
								continue;
							
							nextLanes[tmp.lane()][tmp.pos()] = tmp;
							break;
						case RoadObject.Roadblock:
							nextLanes[i][j] = currLanes[i][j];
							break;
					}
				}
		
		RoadObject[][] t = currLanes;
		currLanes = nextLanes;
		nextLanes = t;
		
		for (int i = 0; i < numOfLanes; i++)
			Arrays.fill(nextLanes[i], null);
	}
	
	public void step(int cnt)
	{
		Vehicle tmp;
		int bfr, aftr;
		
		for (int i = 0; i < numOfLanes; i++)
			for (int j = 0; j < length; j++)
				if (currLanes[i][j] != null)
				{
					switch (currLanes[i][j].type())
					{
						case RoadObject.Vehicle:
							tmp = (Vehicle)currLanes[i][j];
							
							boolean f = true;
							for (SpeedRange sr : speedSigns)
							{
								if (sr.inRange(tmp))
								{
									tmp.speedRangeSign(sr);
									f = false;
									break;
								}
							}	
							if (f)
								tmp.speedRangeSign();
							
							//tmp.transitionFunction();
							tmp.CS_model();
							
							for (LoopDetector ld : loopDetectors)
								ld.passingVehicle(tmp);
							
							for (carCounter cc : carCounters)
								cc.passingVehicle(tmp, cnt);
							
							// jesli warunki brzegowe sa pochlaniajace to position moze byc > length,
							// w takim wypadku samochod nie pojawia sie w kolejnym kroku czasowym
							if (tmp.pos() > length)
								continue;
							
							nextLanes[tmp.lane()][tmp.pos()] = tmp;
							break;
						case RoadObject.Roadblock:
							nextLanes[i][j] = currLanes[i][j];
							break;
					}
				}
		
		RoadObject[][] t = currLanes;
		currLanes = nextLanes;
		nextLanes = t;
		
		for (int i = 0; i < numOfLanes; i++)
			Arrays.fill(nextLanes[i], null);
	}
	
	public trafficData[] loopDetectorsData()
	{
		trafficData[] tmp = new trafficData[loopDetectors.length];
		
		int i = 0;
		for (LoopDetector ld : loopDetectors)
			tmp[i++] = ld.data();
		
		return tmp;
	}
	
	public int length()
	{
		return length;
	}
	
	public int numOfLanes()
	{
		return numOfLanes;
	}
	
	public boolean openBoundaries()
	{
		return open;
	}
	
	public RoadObject[][] lanes()
	{
		return currLanes;
	}
}

class MultiLane
{
	private Road road;
	private int length;
	private int nOfCars;
	private int nOfLanes;
	private int density, flow;
	private double pFault, pSlow, pChange;
	private int count;
	private int dataCycles, dataCount;
	private boolean dataStart;
	private int dataStartValue, carStartValue;
	
	private PrintWriter file;
	
	private carCounter[] counters;
	
	//private int debugging = 0;
	
	MultiLane(int numLanes, int len, double dens, double probFault, double probSlow, double probChange, PrintWriter file)
	{
		Random gen = new Random();
		pFault = probFault;
		pSlow = probSlow;
		pChange = probChange;
		length = len;
		nOfLanes = numLanes;
		
		this.file = file;
		
		dataCycles = 300;
		dataStartValue = 1000;
		dataStart = false;
		dataCount = 0;
		
		carStartValue = 1000;
		
		
		MergeRange[] mergeSigns = new MergeRange[1];
		mergeSigns[0] = new MergeRange(87, 180, 0);
		
		SpeedRange[] speedSigns = new SpeedRange[1];
		speedSigns[0] = new SpeedRange(87, 170, 5, 3, 5);
		
		LoopDetector[] detectors = new LoopDetector[3];
		detectors[0] = new LoopDetector(0, 87, dataCycles);
		detectors[1] = new LoopDetector(1, 87, dataCycles);
		detectors[2] = new LoopDetector(0, 142, dataCycles);
		//detectors[3] = new LoopDetector(0, 180, dataCycles);
		//detectors[4] = new LoopDetector(1, 180, dataCycles);
		
		
		counters = new carCounter[1];
		counters[0] = new carCounter(87, 180, 2000);
		
		
		road = new Road(nOfLanes, len, false, mergeSigns, speedSigns, detectors, dataCycles, counters);
		count = 0;
		
		int blockedRoad = 170 - 140;
		for (int i = 140; i < 170; i++)
		{	
			road.lanes()[1][i] = new RoadObject(RoadObject.Roadblock);
			//road.lanes()[0][i] = new RoadObject(RoadObject.Roadblock);
		}
		
		int cn = nOfCars = (int)(dens * nOfLanes * length) - blockedRoad;
		
		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < nOfLanes; j++)	
				if (road.lanes()[j][i] == null && gen.nextDouble() <= (double)cn / (nOfLanes*length - blockedRoad - i*nOfLanes - j))
				{	
					road.lanes()[j][i] = new Vehicle(road, j, i, pFault, pSlow, pChange);
					cn--;
				}
				//else
					//road.lanes()[j][i] = null;
		}
	}
	
	public byte[][] visual(int beg, int end)
	{
		if (end > road.length())
			end = road.length();
		
		// boolean flag = false;
		
		byte[][] tab = new byte[nOfLanes][end-beg];
		
		for (int i = 0; i < nOfLanes; i++)
		{
			for (int j = beg; j < end; j++)
			{
				if (road.lanes()[i][j] != null)
					if (road.lanes()[i][j].type() == RoadObject.Vehicle)
					{
						// if (debugging == 0)
						// {
							// debugging = ((Vehicle)road.lanes()[i][j]).id();
						// }
						
						
						tab[i][j-beg] = 1;	// Vehicle
						
						// if (((Vehicle)road.lanes()[i][j]).id() == debugging)
						// {
							// flag = true;
							// tab[i][j-beg] = 3;
						// }
						
					}
					else
						tab[i][j-beg] = 2;	// Roadblock
				else
					tab[i][j-beg] = 0;	// empty space
			}
		}
		
		// if (flag == false)
			// debugging = 0;
		
		return tab;
	}
	
	public void step()
	{	
		// Nagel two-lane
		road.subStep();
		
		road.step(count);
		
		if (count == carStartValue)
			counters[0].clear();
		
		if (!dataStart && count >= dataStartValue)
		{	
			dataStart = true;
			road.loopDetectorsData();	// zignoruj dane z czujnikow
		}
		
		if (dataStart && ++dataCount % this.dataCycles == 0)
		{
			trafficData[] data = road.loopDetectorsData();
			
			StringBuilder sb = new StringBuilder();
			
			for (trafficData td : data)
			{
				sb.append(td.density());
				sb.append(',');
				sb.append(td.flow());
				sb.append(',');
				sb.append(td.speed());
				sb.append(',');
				sb.append(td.histogram()[0]);
				sb.append(',');
				sb.append(td.histogram()[1]);
				sb.append(',');
				sb.append(td.histogram()[2]);
				sb.append(',');
				sb.append(td.histogram()[3]);
				sb.append(',');
				sb.append(td.histogram()[4]);
				sb.append(',');
				//System.out.println(sb.toString());
			}
			//sb.setCharAt(sb.length()-1, '\n');
			file.write(sb.toString());
			save(file);
		}
		
		count++;
	}
	
	public void save(PrintWriter plik)
	{	
		StringBuilder sb = new StringBuilder();
		int[][] tab = counters[0].data();
		long sum = 0;
		int num = 0;
		
		for (int i = 0; i < tab[0].length-1; i++)
		{
			sum += i*tab[0][i];
			num += tab[0][i];
		}
		if (num == 0)
		{
			num = 1;
			sum = 0;
		}
		sb.append((double)sum/num);
		sb.append(',');
		
		
		sum = num = 0;
		for (int i = 0; i < tab[1].length-1; i++)
		{
			sum += i*tab[1][i];
			num += tab[1][i];
		}
		if (num == 0)
		{
			num = 1;
			sum = 0;
		}
		sb.append((double)sum/num);
		sb.append(',');
		sb.append(tab[0][tab[0].length-1]);
		sb.append(',');
		sb.append(tab[1][tab[1].length-1]);
		sb.append('\n');
		
		plik.write(sb.toString());
		
		//plik.close();
		//file.close();
	}
	
	public int numOfLanes()
	{
		return nOfLanes;
	}
	
	public int length()
	{
		return length;
	}
}
