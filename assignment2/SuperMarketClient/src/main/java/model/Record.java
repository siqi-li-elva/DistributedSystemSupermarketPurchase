package model;

public class Record {
  // {start time, request type (ie POST), latency, response code}
  private final long startTime;
  private final long endTime;
  private final HttpMethod type;
  private final int resCode;
  private final long latency;

  public Record(long startTime, long endTime, HttpMethod type, int resCode) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.type = type;
    this.resCode = resCode;
    this.latency = endTime - startTime;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public HttpMethod getType() {
    return type;
  }

  public int getResCode() {
    return resCode;
  }

  public long getLatency() {
    return latency;
  }
}
