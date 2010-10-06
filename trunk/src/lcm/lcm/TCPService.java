package lcm.lcm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TCPService
{
    ServerSocket serverSocket;

    AcceptThread acceptThread;
    public ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

    public int bytesCount = 0;

    
    public TCPService (int port) throws IOException {

        serverSocket = new ServerSocket(port   );
        
        acceptThread = new AcceptThread();
        acceptThread.start();

    }

    public void relay(byte channel[], byte data[])
    {
        // synchronously send to all clients.
        String chanstr = new String(channel);
        synchronized(clients) {
            for (ClientThread client : clients) {
                client.send(chanstr, channel, data);
            }
        }
    }

    class AcceptThread extends Thread
    {
        public void run()
        {
            while (true) {
                try {
                    Socket clientSock = serverSocket.accept();

                    ClientThread client = new ClientThread(clientSock);
                    client.start();

                    synchronized(clients) {
                        clients.add(client);
                    }
                } catch (IOException ex) {
                }
            }
        }
    }

    class ClientThread extends Thread
    {
        Socket sock;
        DataInputStream ins;
        DataOutputStream outs;

        class SubscriptionRecord
        {
            String  regex;
            Pattern pat;
            SubscriptionRecord(String regex) {
                this.regex = regex;
                this.pat = Pattern.compile(regex);
            }
        }

        ArrayList<SubscriptionRecord> subscriptions = new ArrayList<SubscriptionRecord>();

        public ClientThread(Socket sock) throws IOException
        {
            this.sock = sock;

            ins = new DataInputStream(sock.getInputStream());
            outs = new DataOutputStream(sock.getOutputStream());

            outs.writeInt(TCPProvider.MAGIC_SERVER);
            outs.writeInt(TCPProvider.VERSION);
        }

        public void run()
        {
            ///////////////////////
            // read messages until something bad happens.
            try {
                while (true) {
                    int type = ins.readInt();
                    if (type == TCPProvider.MESSAGE_TYPE_PUBLISH) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);

                        int datalen = ins.readInt();
                        byte data[] = new byte[datalen];
                        ins.readFully(data);

                        TCPService.this.relay(channel, data);

                        bytesCount += channellen + datalen + 8;
                    } else if(type == TCPProvider.MESSAGE_TYPE_SUBSCRIBE) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        synchronized(subscriptions) {
                            subscriptions.add(new SubscriptionRecord(new String(channel)));
                        }
                    } else if(type == TCPProvider.MESSAGE_TYPE_UNSUBSCRIBE) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        String re = new String(channel);
                        synchronized(subscriptions) {
                            for(int i=0, n=subscriptions.size(); i<n; i++) {
                                if(subscriptions.get(i).regex.equals(re)) {
                                    subscriptions.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {

            }

            ///////////////////////
            // Something bad happened, close this connection.
            try {
                sock.close();
            } catch (IOException ex) {
            }

            synchronized(clients) {
                clients.remove(this);

            }
        }

        public void send(String chanstr, byte channel[], byte data[])
        {
            try {
                synchronized(subscriptions) {
                    for(SubscriptionRecord sr : subscriptions) {
                        if(sr.pat.matcher(chanstr).matches()) {
                            outs.writeInt(TCPProvider.MESSAGE_TYPE_PUBLISH);
                            outs.writeInt(channel.length);
                            outs.write(channel);
                            outs.writeInt(data.length);
                            outs.write(data);
                            outs.flush();
                            return;
                        }
                    }
                }
            } catch (IOException ex) {
            }
        }
    }

    public static void main(String args[])
    {
        try {
            int port = 7700;
            if (args.length > 0)
                port = Integer.parseInt(args[0]);
            new TCPService(port);
        } catch (IOException ex) {
            System.out.println("Ex: "+ex);
        }
    }
}
