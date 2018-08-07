package com.martiansoftware.nailgun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NGCommunicatorTest {
  // Mocks
  private Socket socket;
  private InputStream istream;
  private OutputStream ostream;

  @BeforeEach
  private void before() throws IOException {
    socket = mock(Socket.class);

    istream = mock(InputStream.class);
    ostream = mock(OutputStream.class);

    when(socket.getInputStream()).thenReturn(istream);
    when(socket.getOutputStream()).thenReturn(ostream);
  }

  @Test
  void canRun() throws IOException {
    NGCommunicator comm = new NGCommunicator(socket, 0);
    assertNotNull(comm);
  }

  @Test
  void canReadCommand() throws IOException {
    String command = "some_command";
    byte[] commandBin = command.getBytes(StandardCharsets.UTF_8);
    byte[] payload;
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteStream)) {
      stream.writeInt(commandBin.length);
      stream.writeByte(NGConstants.CHUNKTYPE_COMMAND);
      stream.write(commandBin);
      stream.flush();
      payload = byteStream.toByteArray();
    }

    ByteArrayInputStream istream = new ByteArrayInputStream(payload);
    when(socket.getInputStream()).thenReturn(istream);

    NGCommunicator comm = new NGCommunicator(socket, 0);
    CommandContext context = comm.readCommandContext();
    assertEquals(command, context.getCommand());
  }

  @Test
  void canWriteData() throws IOException {
    NGCommunicator comm = new NGCommunicator(socket, 0);

    byte[] data = {0x01, 0x02, 0x03};
    comm.send(NGConstants.CHUNKTYPE_STDOUT, data, 0, data.length);

    verify(ostream).write(data, 0, data.length);
  }
}
