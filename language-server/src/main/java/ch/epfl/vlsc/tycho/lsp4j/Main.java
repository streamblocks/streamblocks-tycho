package ch.epfl.vlsc.tycho.lsp4j;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        if (args.length == 0) {
            System.out.println("Proper Usage is: TychoLS <port>");
            System.exit(0);
        }


        String port = args[0];
        try {
            CalLanguageServer languageServer = new CalLanguageServer();
            final ServerSocketChannel serverSocket = ServerSocketChannel.open();
            InetSocketAddress _inetSocketAddress = new InetSocketAddress(Integer.parseInt(port));
            serverSocket.bind(_inetSocketAddress);
            final SocketChannel socketChannel = serverSocket.accept();
            InputStream _newInputStream = Channels.newInputStream(socketChannel);
            OutputStream _newOutputStream = Channels.newOutputStream(socketChannel);
            PrintWriter _printWriter = new PrintWriter(System.out);
            final Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(languageServer, _newInputStream, _newOutputStream, true, _printWriter);
            LanguageClient client = launcher.getRemoteProxy();
            languageServer.connect(client);
            launcher.startListening().get();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
