package controllers.auth

import java.net.{URI, URLEncoder}

import controllers.Global
import jp.t2v.lab.play2.auth.social.core.{AccessTokenRetrievalFailedException, OAuth2Authenticator}
import play.api.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.WSResponse
import play.api.mvc.Results
import velocorner.feed.StravaActivityFeed

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * Created by levi on 09/12/15.
  */
class StravaAuthenticator extends OAuth2Authenticator {

  override type AccessToken = String

  override val authorizationUrl: String = StravaActivityFeed.authorizationUrl
  override val clientSecret: String = Global.getSecretConfig.getSecret("strava")
  override val accessTokenUrl: String = StravaActivityFeed.accessTokenUrl
  override val providerName: String = "strava"
  override val clientId: String = Global.getSecretConfig.getId("strava")
  override val callbackUrl: String = Global.getSecretConfig.getCallbackUrl("strava")

  override def getAuthorizationUrl(scope: String, state: String): String = {
    Logger.info(s"authorization url for scope[$scope]")
    val encodedClientId = URLEncoder.encode(clientId, "utf-8")
    // scope is the host name localhost:9000 or www.velocorner.com
    val uri = new URI(callbackUrl)
    val adjustedCallbackUrl = s"${uri.getScheme}://$scope${uri.getPath}"
    val encodedRedirectUri = URLEncoder.encode(adjustedCallbackUrl, "utf-8")
    //val encodedScope = URLEncoder.encode(scope, "utf-8")
    val encodedState = URLEncoder.encode(state, "utf-8")
    s"$authorizationUrl?client_id=$encodedClientId&redirect_uri=$encodedRedirectUri&state=$encodedState&response_type=code&approval_prompt=force&scope=public"
  }

  override def retrieveAccessToken(code: String)(implicit ctx: ExecutionContext): Future[AccessToken] = {
    Logger.info(s"retrieve token for code[$code]")
    Global.getFeed.wsClient.url(accessTokenUrl)
      .withQueryString(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code)
      .withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)
      .post(Results.EmptyContent())
      .map { response =>
        Logger.debug("retrieving access token from provider API: " + response.body)
        parseAccessTokenResponse(response)
      }
  }

  override def parseAccessTokenResponse(response: WSResponse): String = {
    Logger.info("parsing token")
    try {
      (response.json \ "access_token").as[String]
    } catch {
      case NonFatal(e) =>
        throw new AccessTokenRetrievalFailedException(s"Failed to parse access token: ${response.body}", e)
    }
  }
}
