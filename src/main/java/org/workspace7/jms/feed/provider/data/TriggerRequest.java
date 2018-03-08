package org.workspace7.jms.feed.provider.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerRequest {
  private String triggerName;
  private String auth;
  private URI uri;
  private EventPayload eventPayload;
}
