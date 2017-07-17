#
# Script per lanciare l'esempio di simulatore IoT
# Lo script è fatto per essere eseguito nella intranet Oracle (vedi Proxy)
#
# Parametri di input: file di provisioning e relativa pwd (file pwd)
#
# java -cp ./device1.jar:./lib/* -Dhttps.proxyHost=emeacache.uk.oracle.com -Dhttps.proxyPort=80 test/DeviceManager ./lsdevice1 Welcome1

#
# versione NO PROXY
# java -cp ./device1.jar:./lib/* test/DeviceManager ./lsdevice1 Welcome1

java -cp ./device1.jar:./lib/* test/SimpleDeviceManager ./lsdevice1 Welcome1
