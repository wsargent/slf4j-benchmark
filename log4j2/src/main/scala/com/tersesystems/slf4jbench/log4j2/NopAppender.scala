package com.tersesystems.slf4jbench.log4j2

import org.apache.logging.log4j.core.{ Appender, Core, Filter, Layout, LogEvent }
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory

@Plugin(name = "NopAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
class NopAppender(name: String, filter: Filter, layout: Layout[_ <: Serializable])
    extends AbstractAppender(name, filter, layout, false, null) {
  override def append(event: LogEvent): Unit = {}
}

object NopAppender {

  @PluginFactory def createAppender(@PluginAttribute("name") name: String,
                                    @PluginElement("Filter") filter: Filter,
                                    @PluginElement("Layout") layout: Layout[_ <: Serializable]) =
    new NopAppender(name, filter, layout)
}
