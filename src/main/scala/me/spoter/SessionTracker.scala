package me.spoter

import java.net.URI

import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.{Callback, CallbackTo}
import me.spoter.solid_libs.SolidAuth

/**
  * Trait to track a solid session.
  */
trait SessionTracker[P, S, B] {
  protected implicit val stateReuse: Reusability[StateXSession[S]] = Reusability.by_==[StateXSession[S]]

  def trackSession(f: Session => CallbackTo[S])(c: ComponentDidMount[P, StateXSession[S], B]): Callback = Callback {
    SolidAuth.trackSession { s =>
      (if (s != null) {
        val session = Session(new URI(s.webId.toString))
        f(session).flatMap { st =>
          c.modState(os => {
            os.copy(state = st, session = Some(session))
          })
        }
      }
      else
        c.modState(os => os.copyWithSession(None))).runNow()
    }
  }
}

case class StateXSession[S](state: S, session: Option[Session]) {
  type SessionOpt = Option[Session]

  def copyWithSession(s: SessionOpt): StateXSession[S] = copy(session = s)
}

case class Session(webId: URI) extends AnyVal