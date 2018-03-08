package org.workspace7.jms.feed.provider.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;


@Component
@ConfigurationProperties(prefix = "openwhisk")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenWhiskProperties {
  @NotEmpty
  @URL
  private String apiHost;

}
