import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class Client extends JFrame {

	private static final long serialVersionUID = 6917308972322759518L;
	private static final int PORT = 12345;
	private ObjectOutputStream _outputStream;
	private ObjectInputStream _inputStream;
	private Socket _socket;

	private JTextField _textField;					// text box
	private JLabel _nameLabel;						// display username
	private JLabel _helpLabel;						// display help chat command
	private JLabel _connectionLabel;				// display connection status
	private static String userName;;				// store user name


	////CONSTRUCTOR ////
	public Client() {

		// frame defaults
		super("Chat Client by Dylan Secreast");
		setLayout(new BorderLayout());
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		// north panel
		JPanel northPanel = new JPanel();
		_connectionLabel = new JLabel("Connecting to server...");
		_connectionLabel.setHorizontalAlignment(JLabel.CENTER);
		northPanel.add(_connectionLabel);
		_helpLabel = new JLabel();
		_helpLabel.setText("              /? for help");
		_helpLabel.setForeground(Color.GRAY);
		northPanel.add(_helpLabel);

		// south panel
		JPanel southPanel = new JPanel();
		_nameLabel = new JLabel();
		_textField = new JTextField(30);
		_textField.setOpaque(true);
		_textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				clientMsgToServer(_textField.getText());
				_textField.setText("");
			}
		});
		southPanel.add(_nameLabel);
		southPanel.add(_textField);

		// center panel
		JPanel centerPanel = new JPanel();
		JTextArea _textArea = new JTextArea(15, 40);
		_textArea.setEditable(false);
		centerPanel.add(new JScrollPane(_textArea));
		DefaultCaret caret = (DefaultCaret)_textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		// assemble all panels to frame
		add(northPanel, BorderLayout.NORTH);
		add(southPanel, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);
		pack();

		// connect client to server
		try {
//			InetAddress address = InetAddress.getLocalHost();
			InetAddress address = InetAddress.getByName("128.223.4.35");
		
			_socket = new Socket(address, PORT);
			_connectionLabel.setText("Connected to " + address + ":" + PORT);

			_outputStream = new ObjectOutputStream(_socket.getOutputStream());
			_outputStream.flush();

			_inputStream = new ObjectInputStream(_socket.getInputStream());

			setUserName();

			boolean running = true;
			while (running) {
				String msg = _inputStream.readUTF();
				_textArea.append(msg + "\n");
			}
		} catch (IOException ex) {
			// TODO: EOFException thrown
			shutdownClient();
			ex.printStackTrace();
		}
	}	// end Client()

	private void clientMsgToServer(String message) {
		String timeStamp = new SimpleDateFormat("h:mm:ss a").format(new Date());

		if (_outputStream == null) {	// if first outputStream
			return;						// don't send to server
		}

		try {
			switch (message) {
			case "":			// prevent user from sending blank message
				return;
			case "/shutdown":	// shutdown command
				echoNotification("[[" + userName + " has disconnected ]]");
				shutdownClient();
				break;
			case "/name":		// change username command
				String tempName = userName;
				setUserName();
				echoNotification("[[ " + tempName + " has changed their username to " + userName + " ]]");
				break;
			case "/?":			// help command
				echoNotification("[[ " + userName + ", the available chat commands are: /shutdown, /name ]]");
				break;
			case ":party_parrot:":
				echoNotification("The lights dim and electro music starts playing... PARTY PARROT IS OUT TO PLAY!");
				break;
			case ":alex_rules:":
				echoNotification(userName + " would like you all to know Alex is the best!");
				break;
			case ":cyberbully:":
				echoNotification(userName + " would like you to know that Matt still doesn't have an ix account :((((");
					break;
			default:			// send message to server
				_outputStream.writeUTF("[" + timeStamp + "] " + userName + ": " + message);
				_outputStream.flush();
				break;
			}
		} catch (IOException ex) {
			System.err.println("Client Error: Unable to send message. " + ex);
		}
	}	// end sendMessage()

	private void echoNotification(String message) {
		try {
			_outputStream.writeUTF(message);
			_outputStream.flush();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			System.err.println("Client Error: Unable to send message. " + ex);
		}
	}

	private void setUserName() {
		// TODO: limit amount of characters
		userName = JOptionPane.showInputDialog(null, "Enter Username, 3-12 characters.", "User Setup", JOptionPane.QUESTION_MESSAGE);

		if (userName == null) {		// if user clicks cancel button,
			userName = "User";		// assign default username
		}
		else if (userName.length() < 3 || userName.length() > 12) {		// username must be within 3-12 characters
			setUserName();
		}

		_nameLabel.setText(userName + ":");
	}	// end setUserName()

	private void shutdownClient() {
		try {
			_connectionLabel.setForeground(Color.RED);
			_connectionLabel.setText("No server connection");
			_helpLabel.setText("");
			_nameLabel.setText("");
			_textField.setText("Client has disconnected. Unable to send messages.");
			_textField.setEnabled(false);
			_outputStream.flush();
			_inputStream.close();
			_outputStream.close();
			_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//// MAIN ////
	public static void main(String[] args) {
		new Client();
	}
}	// end main()