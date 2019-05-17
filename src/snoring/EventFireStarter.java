package snoring;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class EventFireStarter {

	public static void main(String[] args) {
		/*try {
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("log/console.out")), true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		List<String> fileList = new ArrayList<String>();
		/*
		fileList.add("raw/392327__alienxxx__musical-table.wav");
		fileList.add("raw/264186__deleted-user-4966198__music-box-swan-lake.wav");
		fileList.add("raw/139057__haydensayshi123__green-sleves-music-box.wav");
		fileList.add("raw/273192__rigden33__music208.wav");
		fileList.add("raw/244533__xtrgamr__music-game-disc-eject.wav");
		fileList.add("raw/367603__davidsraba__sounds-like-music.wav");
		*/
		fileList.add("raw/235873__delphidebrain__sjuulke-snoring-1.wav");
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
		//fileList.add("raw/460651__noamp2003__roomtone-kids-talking.wav");
		fileList.add("raw/164606__steveukguy__class-a-female-talking-1.wav");
		fileList.add("raw/61036__timtube__talking-3.wav");
		fileList.add("raw/361928__toiletrolltube__161009-0085-lw-radio.wav");
		fileList.add("raw/61945__noisecollector__radio.wav");
		fileList.add("raw/156220__framixo__radionoize-0.wav");
		fileList.add("raw/69012__lex0myko1__am-147-305mhz.wav");
		//fileList.add("raw/466864__steinhyrningur__door-closing-door-closed.wav");
		//fileList.add("raw/431118__inspectorj__door-front-closing-a.wav");
		fileList.add("raw/205951__ryding__alarm-01.wav");
		fileList.add("raw/93639__benboncan__personal-alarm.wav");
		fileList.add("raw/244917__kwahmah-02__house-alarm.wav");
		fileList.add("raw/345230__embracetheart__ceiling-fan-indoor.wav");
		fileList.add("raw/435518__sromon__swtch-and-start-the-fan-ambiance.wav");
		fileList.add("raw/40621__acclivity__sleepingbeauty.wav");
		fileList.add("raw/77267__sagetyrtle__catsnores.wav");
		fileList.add("raw/180535__suz-soundcreations__footsteps-snow-mono-44-16.wav");
		fileList.add("raw/259639__stevious42__footsteps-in-street-woman.wav");
		fileList.add("raw/63103__robinhood76__00555-snoring-1-heavy-breath.wav");
		fileList.add("raw/69329__robinhood76__00966-baby-snoring-2.wav");
		fileList.add("raw/61605__andune__schnauf.wav");
		fileList.add("raw/235873__delphidebrain__sjuulke-snoring-1.wav");
		//fileList.add("raw/20545__sirplus__snore.wav");
		fileList.add("raw/377119__ejking17__20170112-the-zzz.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/Matt Script - TEETH GRINDING_GRIND_TOOTH_CREAK.wav");
		fileList.add("raw/Matt Script - TEETH CHATTERING_CHATTER_TOOTH.wav");
		fileList.add("raw/Matt Script - TEETH CHATTERING_FAST_MOVEMENT_MOUTH OPEN.wav");
		fileList.add("raw/20545__sirplus__snore.wav");
		fileList.add("raw/veryclearrepeating.wav");
		//fileList = new ArrayList<String>();
		fileList.add("raw/Snoring vs Sleep Apnea  - What the difference sounds like.wav");
		fileList.add("raw/What does Sleep Apnea sound like.wav");
		for(String filePath : fileList) {
			new EventFireGui(filePath);
		}

	}

}
