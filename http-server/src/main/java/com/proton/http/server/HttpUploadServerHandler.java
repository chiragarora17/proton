package com.proton.http.server;

import static io.netty.handler.codec.http.HttpMethod.POST;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

/**
 * Handles uploading of file and then saves it to a known location.
 */
public class HttpUploadServerHandler extends ChannelInboundHandlerAdapter {

  // Factory that writes to disk
  private static final HttpDataFactory factory = new DefaultHttpDataFactory(true);
  private static final String FILE_UPLOAD_LOCN = "/tmp/uploads/";
  private HttpRequest httpRequest;
  private HttpPostRequestDecoder httpDecoder;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      List<String> output = new ArrayList<>();
    FullHttpRequest request = (FullHttpRequest) msg;
    if (request instanceof HttpRequest) {
        httpRequest = (HttpRequest) request;
        final URI uri = new URI(httpRequest.getUri());

        System.out.println("Got URI " + uri);
        if (httpRequest.getMethod() == POST) {
          httpDecoder = new HttpPostRequestDecoder(factory, httpRequest);
          httpDecoder.setDiscardThreshold(0);
        }
      }
      if (httpDecoder != null) {
        if (request instanceof HttpContent) {
          final HttpContent chunk = (HttpContent) request;
          httpDecoder.offer(chunk);
          readChunk(httpDecoder, output);
          if(output.size()> 0) {
            ctx.channel().attr(Constants.PATH_ATTRIBUTE()).set(output.get(0));
          }
          if (chunk instanceof LastHttpContent) {
            resetPostRequestDecoder();
          }
        }
      }
    super.channelRead(ctx, msg);
  }

  //
  //@Override
  //public Map<String, String> channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request)
  //  throws Exception {
  //  Map<String, String> output = new HashMap<>();
  //  if (request instanceof HttpRequest) {
  //    httpRequest = (HttpRequest) request;
  //    final URI uri = new URI(httpRequest.getUri());
  //
  //    System.out.println("Got URI " + uri);
  //    if (httpRequest.getMethod() == POST) {
  //      httpDecoder = new HttpPostRequestDecoder(factory, httpRequest);
  //      httpDecoder.setDiscardThreshold(0);
  //    }
  //  }
  //  if (httpDecoder != null) {
  //    if (request instanceof HttpContent) {
  //      final HttpContent chunk = (HttpContent) request;
  //      httpDecoder.offer(chunk);
  //      readChunk(httpDecoder, ctx, output);
  //
  //      if (chunk instanceof LastHttpContent) {
  //        resetPostRequestDecoder();
  //      }
  //    }
  //  }
  //  return output;
  //}
  //
  private static void readChunk(HttpPostRequestDecoder httpDecoder, List<String> output)
    throws
    IOException {
    boolean val = false;
    boolean visited = false;
    while (httpDecoder.hasNext()) {
      InterfaceHttpData data = httpDecoder.next();
      if (data != null) {
        try {
          switch (data.getHttpDataType()) {
          case Attribute:
            val = true;
            break;
          case FileUpload:
            visited = true;
            final FileUpload fileUpload = (FileUpload) data;
            final File file = new File(FILE_UPLOAD_LOCN + fileUpload.getFilename());
            if (!file.exists()) {
              file.createNewFile();
            }
            System.out.println("Created file " + file);
            try (FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                 FileChannel outputChannel = new FileOutputStream(file).getChannel()) {
              outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
              output.add(file.getAbsolutePath());
              //sendResponse(ctx, CREATED, "file name: " + file.getAbsolutePath());
            }
            break;
          }
        } finally {
          data.release();
        }
      }
      if (visited || val) {
        break;
      }
    }
  }

  ///**
  // * Sends a response back.
  // * @param ctx
  // * @param status
  // * @param message
  // */
  //private static void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
  //  final FullHttpResponse response;
  //  String msgDesc = message;
  //  if (message == null) {
  //    msgDesc = "Failure: " + status;
  //  }
  //  msgDesc += " \r\n";
  //
  //  final ByteBuf buffer = Unpooled.copiedBuffer(msgDesc, CharsetUtil.UTF_8);
  //  if (status.code() >= HttpResponseStatus.BAD_REQUEST.code()) {
  //    response = new DefaultFullHttpResponse(HTTP_1_1, status, buffer);
  //  } else {
  //    response = new DefaultFullHttpResponse(HTTP_1_1, status, buffer);
  //  }
  //  response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
  //
  //  // Close the connection as soon as the response is sent.
  //  ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
  //}

  private void resetPostRequestDecoder() {
    httpRequest = null;
    httpDecoder.destroy();
    httpDecoder = null;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    System.out.println("Got exception " + cause);
    ctx.channel().close();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (httpDecoder != null) {
      httpDecoder.cleanFiles();
    }
  }

}