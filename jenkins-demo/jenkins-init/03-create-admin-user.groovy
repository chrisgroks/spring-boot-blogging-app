import jenkins.model.*
import hudson.security.*
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.FullControlOnceLoggedInStrategy
import jenkins.install.InstallState

def instance = Jenkins.getInstance()

// Create admin user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin123")
instance.setSecurityRealm(hudsonRealm)

// Set authorization strategy
def strategy = new FullControlOnceLoggedInStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

// Mark setup as complete
if (!instance.installState.isSetupComplete()) {
    InstallState.INITIAL_SETUP_COMPLETED.initializeState()
}

instance.save()

println("Admin user created: admin/admin123")
