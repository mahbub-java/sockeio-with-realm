# sockeio-with-realm
Socket IO implementation With realm database 

Need to change socket IO node server URI first from SocketManager

# Change your uri
If you test from adb you can use 
mSocket = IO.socket("http://10.0.2.2:3000") as uri

If you test from real device you will give your node js uri, where node running. If you use local host for node js server. Get your router IP address from ipconfig, it will be like..
mSocket = IO.socket("http://192.168.1.105:3000") //here 192.168.1.105 is my pc IP.

Sometime you can block by firewall. You can set inbound and outbound rule from advance security of port 3000 (Which is used in this project) and allow to comminicate.

# Add user anytime
Only limited user available, login with (1234/1235/1236/1237)
Those user data came from REALM database, anytime extens by input extra user from LoginActivity and ready to check.

# How to start
When you testing please login from two different devices and login with two different ID and choose them each other to start chat.
Message once deliver will available in offline.
