public class combined {
	
	public static void main(String[] args) {

		Thread videoComponent = new Thread (new video());
		videoComponent.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.exit(0);
		}
		Thread audioComponent = new Thread (new audio());
		audioComponent.start();
		
	}

}
