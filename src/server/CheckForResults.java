package server;

import java.util.TimerTask;

/**
 *
 * @author Catarina Teodoro
 */

public class CheckForResults extends TimerTask {

	private DealWithClient client;

	public CheckForResults(DealWithClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		client.sendResults();
	}

}
