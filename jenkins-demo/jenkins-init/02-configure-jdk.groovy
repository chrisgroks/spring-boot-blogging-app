import hudson.model.*
import hudson.tools.*
import jenkins.model.*
import hudson.tools.JDKInstaller
import hudson.tools.InstallSourceProperty

def instance = Jenkins.getInstance()

// Configure JDK 23 (Zulu distribution to match GHA)
def jdkInstaller = new JDKInstaller("23.0.1+11", true)
def jdk = new JDK("JDK-23", "", [
    new InstallSourceProperty([jdkInstaller])
])

def jdkDescriptor = instance.getDescriptor("hudson.model.JDK")
jdkDescriptor.setInstallations(jdk)
jdkDescriptor.save()

println("JDK-23 configured successfully")

instance.save()
