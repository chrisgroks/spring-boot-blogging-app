import hudson.model.*
import hudson.tools.*
import jenkins.model.*
import hudson.tools.JDKInstaller
import hudson.tools.InstallSourceProperty

def instance = Jenkins.getInstance()

// Configure JDK 17 (Zulu distribution to match GHA)
def jdkInstaller = new JDKInstaller("17.0.1+12", true)
def jdk = new JDK("JDK-17", "", [
    new InstallSourceProperty([jdkInstaller])
])

def jdkDescriptor = instance.getDescriptor("hudson.model.JDK")
jdkDescriptor.setInstallations(jdk)
jdkDescriptor.save()

println("JDK-17 configured successfully")

instance.save()
