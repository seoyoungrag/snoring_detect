package snoring;

import java.util.ArrayList;
import java.util.List;

public class EventFireStarter {

	public static void main(String[] args) {
		/*
		try {
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("log/console6.out")), true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/
		List<String> fileList = new ArrayList<String>();
		/*
		fileList.add("raw/392327__alienxxx__musical-table.wav");
		fileList.add("raw/264186__deleted-user-4966198__music-box-swan-lake.wav");
		fileList.add("raw/139057__haydensayshi123__green-sleves-music-box.wav");
		fileList.add("raw/273192__rigden33__music208.wav");
		fileList.add("raw/244533__xtrgamr__music-game-disc-eject.wav");
		fileList.add("raw/367603__davidsraba__sounds-like-music.wav");
		*/
		fileList.add("raw/401334__ckvoiceover__man-coughing.wav");
		fileList.add("raw/178997__bigtexan7213__coughing.wav");
		fileList.add("raw/252240__reitanna__real-cough.wav");
		fileList.add("raw/41386__sandyrb__db-cough-002.wav");
		fileList.add("raw/348364__frostyfrost__cough.wav");
		fileList.add("raw/155858__rutgermuller__footsteps-in-factory-hall-on-wood-and-concrete.wav");
		fileList.add("raw/194825__macphage__gravel3.wav");
		fileList.add("raw/180535__suz-soundcreations__footsteps-snow-mono-44-16.wav");
		fileList.add("raw/259639__stevious42__footsteps-in-street-woman.wav");
		fileList.add("raw/267499__purplewalrus23__footsteps-on-rough-gravel.wav");
		fileList.add("raw/175954__freefire66__footsteps.wav");
		fileList.add("raw/195132__philter137__walking-from-wood-to-path.wav");
		fileList.add("raw/259646__stevious42__footsteps-in-the-street.wav");
		fileList.add("raw/48212__slothrop__footsteps.wav");
		fileList.add("raw/198962__mydo1__footsteps-on-wood.wav");
		fileList.add("raw/223152__yoyodaman234__glass-footstep-1.wav");
		fileList.add("raw/238608__shart69__talking-creature.wav");
		fileList.add("raw/428777__pauliperez1999__bimbo-girl-3.wav");
		fileList.add("raw/164606__steveukguy__class-a-female-talking-1.wav");
		fileList.add("raw/61036__timtube__talking-3.wav");
		fileList.add("raw/361928__toiletrolltube__161009-0085-lw-radio.wav");
		fileList.add("raw/61945__noisecollector__radio.wav");
		fileList.add("raw/156220__framixo__radionoize-0.wav");
		fileList.add("raw/69012__lex0myko1__am-147-305mhz.wav");
		fileList.add("raw/205951__ryding__alarm-01.wav");
		fileList.add("raw/93639__benboncan__personal-alarm.wav");
		fileList.add("raw/244917__kwahmah-02__house-alarm.wav");
		fileList.add("raw/345230__embracetheart__ceiling-fan-indoor.wav");
		fileList.add("raw/435518__sromon__swtch-and-start-the-fan-ambiance.wav");
		fileList.add("raw/180535__suz-soundcreations__footsteps-snow-mono-44-16.wav");
		fileList.add("raw/259639__stevious42__footsteps-in-street-woman.wav");
		//fileList = new ArrayList<String>();
		//fileList = new ArrayList<String>();
		//fileList = new ArrayList<String>();
		//fileList.add("raw/2019_05_30_03_27_48.wav");

		//fileList = new ArrayList<String>();
		//fileList = new ArrayList<String>();
		fileList.add("raw/s107FY1JHHyr.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/s107FY1JHHyr-영역-010.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/veryclearrepeating-영역-011.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/Matt Script - SINGLE BITE WITH TEETH_HUMAN_BITING-영역-012.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/114609__daxter31__snoring-영역-013.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/63103__robinhood76__00555-snoring-1-heavy-breath-영역-014.wav");
		//fileList = new ArrayList<String>();
		//fileList = new ArrayList<String>();
		fileList.add("raw/Snoring vs Sleep Apnea  - What the difference sounds like.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/beep-01a.wav");
		
		//fileList = new ArrayList<String>();
		//이갈이 파일들
		//fileList = new ArrayList<String>();
		fileList.add("raw/veryclearrepeating.wav");
		fileList.add("raw/Matt Script - TEETH GRINDING_GRIND_TOOTH_CREAK.wav");
		fileList.add("raw/Matt Script - TEETH CHATTERING_CHATTER_TOOTH.wav");
		fileList.add("raw/Matt Script - TEETH CHATTERING_FAST_MOVEMENT_MOUTH OPEN.wav");
		//fileList = new ArrayList<String>();
		//fileList.add("raw/veryclearrepeating.wav");
		//fileList.add("raw/114609__daxter31__snoring.wav");
		//fileList.add("raw/114609__daxter31__snoring-영역-016.wav");
		//fileList.add("raw/veryclearrepeating-영역-017.wav");
		//fileList = new ArrayList<String>();
		//fileList.add("raw/s107FY1JHHyr.wav");
		fileList = new ArrayList<String>();
		fileList.add("raw/What does Sleep Apnea sound like.wav");
		fileList = new ArrayList<String>();
		fileList.add("raw/What does Sleep Apnea sound like.wav");
		fileList.add("raw/Snoring vs Sleep Apnea  - What the difference sounds like.wav");
		fileList = new ArrayList<String>();
		fileList.add("raw/snoring-201906_12_2046_12_2047_1560340067330.wav");
		fileList.add("raw/snoring-201906_12_2050_12_2051_1560340282024.wav");
		fileList.add("raw/snoring-201906_12_2059_12_2100_1560340849079.wav");
		fileList = new ArrayList<String>();
		fileList.add("raw/165395__shawnyboy__white-noise.wav");
		//코골이 파일들
		fileList = new ArrayList<String>();
		fileList.add("raw/114609__daxter31__snoring.wav");
		fileList.add("raw/20545__sirplus__snore.wav");
		fileList.add("raw/235873__delphidebrain__sjuulke-snoring-1.wav");
		fileList.add("raw/377119__ejking17__20170112-the-zzz.wav");
		fileList.add("raw/40621__acclivity__sleepingbeauty.wav");
		fileList.add("raw/61605__andune__schnauf.wav");
		fileList.add("raw/63103__robinhood76__00555-snoring-1-heavy-breath.wav");
		fileList.add("raw/69329__robinhood76__00966-baby-snoring-2.wav");
		fileList.add("raw/Snoring vs Sleep Apnea  - What the difference sounds like.wav");
		fileList = new ArrayList<String>();
		fileList.add("raw/snoring-201906_12_2046_12_2047_1560340067330.wav");
		fileList.add("raw/snoring-201906_12_2050_12_2051_1560340282024.wav");
		fileList.add("raw/snoring-201906_12_2059_12_2100_1560340849079.wav");
		fileList = new ArrayList<String>();
		fileList.add("raw/s107FY1JHHyr.wav");
		fileList = new ArrayList<String>();
		//fileList.add("raw/snoring-201906_19_2328_20_0111_1560960682552.wav");
		//fileList.add("raw/snoring-201906_20_0144_20_0230_1560965431614.wav");
		//fileList.add("raw/snoring-201906_20_0245_20_0315_1560968119965.wav");
		fileList.add("raw/1139_extended.wav");
		for(String filePath : fileList) {
			//new RecordingThread(filePath);
			RecordFragment rf= new RecordFragment();
			rf.setFilePath(filePath);
			rf.start();
		}

	}

}
