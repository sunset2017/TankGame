package TankGame18;
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class HistoryRecorder
{
	List<ExpUnit> battleHistory;
	public HistoryRecorder()
	{
		battleHistory=new ArrayList<ExpUnit>();
	}
	
	public void saveToCSVfile()
	{
		String address="/Users/haopan/Desktop/HeroGameHistory.csv";
		CSVWriter cw=new CSVWriter(address);
		cw.writeToCSV(battleHistory);
		battleHistory.clear();
	}
	
	public void addBattleHistory()
	{
		Vector<Tank> alltanks=MainGame.allTanks;
		
		if(alltanks.size()<6)
		{
			System.out.println("Not enough tanks for recording!");
			return;
		}
		Vector<Bullet> allbullets=MainGame.allBullets;
		List<Integer> stepHis=new ArrayList<Integer>();
		Hero tk=null;
		try{
		tk=(Hero) alltanks.get(0);
		stepHis.add(Helper.coordinateToUnitIndex(tk.position));
		stepHis.add(tk.direction);
		if(tk.fireEnemy==true)
			stepHis.add(1);
		else
			stepHis.add(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("fileWriter: line 40 !!!!Hero is died, not be able to record history!!!!");
			return;
		}
		//record enemy information.
		int i;
		for(i=1;i<alltanks.size();i++)
		{
			Tank etk=alltanks.get(i);
			if(etk.isAlive())
			{
			stepHis.add(Helper.coordinateToUnitIndex(etk.position));
			stepHis.add(etk.direction);
			}
		}
		
		if(stepHis.size()<ExpUnit.TOTAL_TANK_INFO)
		{
			System.out.println("Error(Adding battle History): not enough Tank recorded!: "+stepHis.size());
		}
		
		Vector<Bullet> get4nearestBullets=getKNearestBullets(3,tk.position,allbullets);
		//record Bullet.
		for(i=0;i<get4nearestBullets.size();i++)
		{
			Bullet bt=get4nearestBullets.get(i);
			if(bt.isLive())
			    stepHis.add(Helper.pixelPositionToUnitIndex(bt.getX(), bt.getY()));
			else
			{
				stepHis.add(-1);
				System.out.println("Bullet is not enough");
			}
		}
		
		// add to battle history.
		ExpUnit eu=new ExpUnit(stepHis);
		battleHistory.add(eu);
	}
	
	public Vector<Bullet> getKNearestBullets(int k,Coordinate myPosition, Vector<Bullet> allbullets)
	{
		PriorityQueue<Bullet> pq=new PriorityQueue<Bullet>(new Comparator<Bullet>(){
			@Override
			public int compare(Bullet o1, Bullet o2) {
				// TODO Auto-generated method stub
				int distance1=Helper.getManhattanDistance(myPosition,Helper.pixelPositiontoGridPosition(o1.getX(), o1.getY()));
				int distance2=Helper.getManhattanDistance(myPosition,Helper.pixelPositiontoGridPosition(o2.getX(), o2.getY()));
				return distance1-distance2;
			}
		});
		
		for(int i=0;i<allbullets.size();i++)
		{
			Bullet bt=allbullets.get(i);
			if(bt.getType()=='E' && bt.isLive())
			{
				pq.add(bt);
			}
		}
		
		Vector<Bullet> kNest=new Vector<Bullet>();
		for(int i=0;i<k;i++)
		{
			if(pq.size()>0)
			{
				Bullet e= pq.poll();
				kNest.add(e);
			}
			else
			{
				Bullet e=new Bullet("Create Default Bullet");
				kNest.add(e);
			}
		}		
		return kNest;
	}
	
	public void calculateMyUtility(int startUtility)
	{
		double discountFactor=0.8;
		for(int i=battleHistory.size()-1;i>=0;i--)
		{
			ExpUnit eu=battleHistory.get(i);
			if(i==battleHistory.size()-1)
			{
				eu.addInfo(startUtility);
				//System.out.println("the current utility is: "+startUtility);
			}
			else
			{
				ExpUnit earlierUnit=battleHistory.get(i+1);
				int utility=(int)(earlierUnit.utility*discountFactor-1);
				eu.addInfo(utility);
			}
		}
	}
}


class ExpUnit
{
	List<Integer> information;
	final static int TOTAL_TANK_INFO=3+2*5;
	int utility=Integer.MIN_VALUE;
	static int count=-1;
	
	public ExpUnit(List<Integer> unit)
	{
		this.information=unit;
		count++;
		//System.out.println("Unit: "+count);
	}
	
	public void addInfo(int u)
	{
		if(utility!=Integer.MIN_VALUE)
		{
			System.out.println("This unit has already been calculated utility!");
		}else
		{
			this.information.add(u);
		    this.utility=u;
		}
	}
}

class CSVWriter {
	private static final String COMMA_SIGN=",";
	private static final String NEW_LINE_SPE="\n";
	private static String File_Header="MyPosition,Dire,Fire,e1Posi,e1Dire,e2Posi,e2Dire,e3Posi,e3Dire,e4Posi,e4Dire,e5Posi,e5Dire,Bullets,Bullets,Bullets,Utility";
	private FileWriter fw= null;
	private String fileName=null;
	static boolean Append_Flag=false;
	
	public CSVWriter(String fileName)
	{
		this.fileName=fileName;
	}
	
	public void setFileHeader(String header)
	{
		CSVWriter.File_Header=header;
	}
	
	public void writeToCSV(List<ExpUnit> data)
	{
		try{
		    fw=new FileWriter(fileName,Append_Flag);
		    if(Append_Flag==false)
		    {
		    	fw.append(File_Header.toString());
		    	fw.append(NEW_LINE_SPE);
		    	Append_Flag=true;
		    }
		    for(int u_i=0;u_i<data.size();u_i++)
		    {
		    	ExpUnit u=data.get(u_i);
		    	List<Integer> allInfo=u.information;
		    	for(int i=0;i<allInfo.size();i++)
		    	{
		    		int value=allInfo.get(i);
		    		fw.append(String.valueOf(value));
		    		if(i!=allInfo.size()-1)
		    		{
		    			fw.append(COMMA_SIGN);
		    		}
		    	}
		    	fw.append(NEW_LINE_SPE);
		    }
		    //System.out.println("CSV file has been written sucessfully!");
		    
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally
		{
			try {
				//System.out.println("file has been closed!");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
