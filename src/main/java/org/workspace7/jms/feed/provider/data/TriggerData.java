package org.workspace7.jms.feed.provider.data;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TriggerData {

  @Expose
  private String authKey;
  @Expose
  private String destinationName;
  @Expose
  private String triggerName;
  @Expose
  private String triggerShortName;
  @Expose
  @SerializedName("_rev")
  private String revision;
}
