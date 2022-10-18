import java.util.ArrayList;

public class RequestRateTracker {
    
    private ArrayList<Long> requestTimestamps;
    private boolean enabled;
    final int recordingWindow;

    public RequestRateTracker(int recordingWindowInSeconds)
    {
        this.recordingWindow = recordingWindowInSeconds;
        this.requestTimestamps = new ArrayList<>();
    }

    public void Enable()
    {
        this.requestTimestamps.clear();
        this.enabled = true;
    }

    public void Disable()
    {
        this.enabled = false;
        this.requestTimestamps.clear();
    }

    public void NewRequest()
    {
        if (!this.enabled)
            return;
        
        this.requestTimestamps.add( System.currentTimeMillis() / 1000 );
        this.ValidateRequestTimestamps();
    }

    private void ValidateRequestTimestamps()
    {
        int i = 0;
        long now = System.currentTimeMillis() / 1000;
        while (i < this.requestTimestamps.size())
        {
            // has the request expired for the recording window
            if ( now - this.requestTimestamps.get(i) > this.recordingWindow )
            {
                this.requestTimestamps.remove(i);
                i--;
            }

            i++;
        }
    }

    public int RequestsInWindow()
    {
        return this.requestTimestamps.size();
    }

}
