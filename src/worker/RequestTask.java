package worker;

import java.util.TimerTask;

/**
 *
 * @author Catarina Teodoro
 */

public class RequestTask extends TimerTask {

	private Worker worker;

	public RequestTask(Worker worker) {
		this.worker = worker;
	}

	@Override
	public void run() {
		worker.requestTask();
	}

}
