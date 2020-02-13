package edu.cuc.readnfctag2share.backends;

import android.os.AsyncTask;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;

import edu.cuc.readnfctag2share.CCCGrpc;
import edu.cuc.readnfctag2share.Content;
import edu.cuc.readnfctag2share.helpers.ClipboardHelper;
import io.grpc.ManagedChannel;

public class RPCHandler {

    private static final String TAG = RPCHandler.class.getSimpleName();

    private interface GrpcRunnable {
        String run(CCCGrpc.CCCBlockingStub blockingStub, CCCGrpc.CCCStub asyncStub) throws Exception;
    }

    public static class ShareClipBoardRunnable implements GrpcRunnable {

        @Override
        public String run(CCCGrpc.CCCBlockingStub blockingStub, CCCGrpc.CCCStub asyncStub) throws Exception {
            Content request = Content.newBuilder().setContent("test data from android").build();
            Content content;
            content = blockingStub.shareClipBoard(request);
            if (content != null && !content.getContent().isEmpty())
                return content.getContent();
            else
                return "";
        }
    }

    public static class GrpcTask extends AsyncTask<Void, Void, String> {
        private final GrpcRunnable grpcRunnable;
        private final ManagedChannel channel;
        private final WeakReference<BackendService> serviceReference;

        public GrpcTask(GrpcRunnable grpcRunnable, ManagedChannel channel, BackendService service) {
            this.grpcRunnable = grpcRunnable;
            this.channel = channel;
            this.serviceReference = new WeakReference<BackendService>(service);
        }

        @Override
        protected String doInBackground(Void... nothing) {
            try {
                String logs =
                        grpcRunnable.run(CCCGrpc.newBlockingStub(channel), CCCGrpc.newStub(channel));
                return "Success!\n" + logs;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return "Failed... :\n" + sw;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            BackendService service = serviceReference.get();
            if (service == null) {
                return;
            }
            // TODO:
            Log.i(TAG, "onPostExecute: " + result);
        }
    }
}
