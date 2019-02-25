package me.spoter

import java.net.URI

import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.{Callback, CallbackTo}
import me.spoter.solid_libs.SolidAuth

import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Trait to track a solid session.
  */
trait SessionTracker[P, S, B] {
  private var loginCallback: Option[js.Function1[js.Dynamic, Unit]] = None
  private var logoutCallback: Option[js.Function1[js.Dynamic, Unit]] = None

  protected implicit val stateReuse: Reusability[StateXSession[S]] = Reusability.by_==[StateXSession[S]]

  protected def trackSessionOn(f: Session => CallbackTo[S])(c: ComponentDidMount[P, StateXSession[S], B]): Callback = Callback {
    if (loginCallback.isEmpty) loginCallback = Some(onLogin(f, c))
    SolidAuth.addListener("login", loginCallback.get)

    if (logoutCallback.isEmpty) logoutCallback = Some(onLogout(c))
    SolidAuth.addListener("logout", logoutCallback.get)

    sessionInBackground(f, c)
    println(s"Login Listener Count After add = ${SolidAuth.listenerCount("login")}")
    println(s"Logout Listener Count After add = ${SolidAuth.listenerCount("logout")}")
  }

  protected def trackSessionOff(): Callback = Callback {
    loginCallback.foreach(SolidAuth.removeListener("login", _))
    loginCallback = None
    logoutCallback.foreach(SolidAuth.removeListener("logout", _))
    logoutCallback = None

    println(s"Login Listener Count After remove = ${SolidAuth.listenerCount("login")}")
    println(s"Logout Listener Count After remove = ${SolidAuth.listenerCount("logout")}")
  }

  private def sessionInBackground(f: Session => CallbackTo[S], c: ComponentDidMount[P, StateXSession[S], B]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    SolidAuth
      .currentSession()
      .toFuture
      .onComplete {
        case Success(s) =>
          if (s != null) {
            onLogin(f, c)(s)
          }
          else {
            onLogout(c)(s)
          }
        case Failure(e) =>
          println(s"Got error when trying to get the current session! ${e.getMessage}")
          c.modState(os => os.copy(session = None)).runNow()
      }
  }

  private def onLogin(f: Session => CallbackTo[S], c: ComponentDidMount[P, StateXSession[S], B]): js.Function1[js.Dynamic, Unit] =
    s => {
      val session = Session(new URI(s.webId.toString))
      f(session).flatMap { st =>
        c.modState(os => {
          os.copy(state = st, session = Some(session))
        })
      }.runNow()
    }

  private def onLogout(c: ComponentDidMount[P, StateXSession[S], B]): js.Function1[js.Dynamic, Unit] =
    _ => c.modState(os => os.copy(session = None)).runNow()
}

case class StateXSession[S](state: S, session: Option[Session])

case class Session(webId: URI) extends AnyVal