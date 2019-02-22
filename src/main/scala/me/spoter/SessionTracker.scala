package me.spoter

import java.net.URI

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import me.spoter.solid_libs.SolidAuth

/**
  * Trait to track a solid session.
  */
trait SessionTracker[P, S, B] {
  def trackSession(c: ComponentDidMount[P, StateXSession[S], B]): Callback = Callback {
    SolidAuth.trackSession { s =>
      (if (s != null)
        c.modState(os => os.copyWithSession(Some(Session(new URI(s.webId.toString)))))
      else
        c.modState(os => os.copyWithSession(None))).runNow()
    }
  }
}

case class StateXSession[S](s: S, session: Option[Session]) {
  type SessionOpt = Option[Session]

  def copyWithSession(s: SessionOpt): StateXSession[S] = copy(session = s)
}

case class Session(webId: URI) extends AnyVal