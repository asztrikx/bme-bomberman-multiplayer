#!/bin/bash
sudo iptables -A INPUT -p tcp -m conntrack --ctstate NEW -m tcp --dport 32469 -j ACCEPT
java -jar NHF.jar --server --config-file config.json