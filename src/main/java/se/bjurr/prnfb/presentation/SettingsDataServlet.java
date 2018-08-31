package se.bjurr.prnfb.presentation;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static se.bjurr.prnfb.transformer.SettingsTransformer.toDto;
import static se.bjurr.prnfb.transformer.SettingsTransformer.toPrnfbSettingsData;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.google.common.base.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import se.bjurr.prnfb.presentation.dto.SettingsDataDTO;
import se.bjurr.prnfb.service.SettingsService;
import se.bjurr.prnfb.service.UserCheckService;
import se.bjurr.prnfb.settings.PrnfbSettingsData;
import se.bjurr.prnfb.settings.Restricted;
import se.bjurr.prnfb.settings.USER_LEVEL;

@Path("/settings")
public class SettingsDataServlet {
  private final SettingsService settingsService;
  private final UserCheckService userCheckService;

  public SettingsDataServlet(UserCheckService userCheckService, SettingsService settingsService) {
    this.userCheckService = userCheckService;
    this.settingsService = settingsService;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get() {
    if (!this.userCheckService.isViewAllowed()) {
      return status(UNAUTHORIZED).build();
    }

    final PrnfbSettingsData settingsData = this.settingsService.getPrnfbSettingsData();
    final SettingsDataDTO settingsDataDto = toDto(settingsData);

    return ok(settingsDataDto).build();
  }

  @POST
  @XsrfProtectionExcluded
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response post(SettingsDataDTO settingsDataDto) {
    final USER_LEVEL adminRestriction =
        settingsService.getPrnfbSettingsData().getAdminRestriction();
    if (!this.userCheckService.isAdminAllowed(
        new Restricted() {
          @Override
          public Optional<String> getRepositorySlug() {
            return Optional.absent();
          }

          @Override
          public Optional<String> getProjectKey() {
            return Optional.absent();
          }
        },
        adminRestriction)) {
      return status(UNAUTHORIZED).build();
    }

    final PrnfbSettingsData prnfbSettingsData = toPrnfbSettingsData(settingsDataDto);
    this.settingsService.setPrnfbSettingsData(prnfbSettingsData);

    return noContent().build();
  }
}
