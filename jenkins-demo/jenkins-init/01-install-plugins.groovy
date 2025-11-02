import jenkins.model.*
import java.util.logging.Logger

def logger = Logger.getLogger("")
def installed = false
def initialized = false

def pluginManager = Jenkins.instance.pluginManager
def updateCenter = Jenkins.instance.updateCenter

// List of plugins required for the demo
def plugins = [
    "git",
    "workflow-aggregator",
    "pipeline-stage-view",
    "junit",
    "checkstyle",
    "warnings-ng",
    "htmlpublisher",
    "gradle",
    "timestamper",
    "ws-cleanup",
    "github",
    "github-branch-source"
]

logger.info("Installing required plugins for Jenkins migration demo...")

plugins.each { pluginName ->
    if (!pluginManager.getPlugin(pluginName)) {
        logger.info("Installing plugin: ${pluginName}")
        def plugin = updateCenter.getPlugin(pluginName)
        if (plugin) {
            plugin.deploy(true)
            installed = true
        }
    } else {
        logger.info("Plugin already installed: ${pluginName}")
    }
}

if (installed) {
    logger.info("Plugins installed, restart required")
    Jenkins.instance.safeRestart()
}
