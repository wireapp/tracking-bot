package com.wire.bots.tracking;

import com.wire.xenon.models.AttachmentMessage;
import com.wire.xenon.tools.Logger;
import org.bouncycastle.cms.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class Tools {
    public static boolean verify(byte[] doc, byte[] sig) throws CMSException, NoSuchAlgorithmException {
        boolean ret = false;
        Provider provider = Security.getProvider("BC");
        CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(doc), sig);
        // final X509Store certStore = cms.getCertificates("Collection", provider);
        CertStore certStore = cms.getCertificatesAndCRLs("Collection", provider);

        for (Object signerO : cms.getSignerInfos().getSigners()) {
            SignerInformation signer = (SignerInformation) signerO;
            final SignerId sid = signer.getSID();
            try {
                final Collection matches = certStore.getCertificates(sid);
                for (Object certO : matches) {
                    final X509Certificate cert = (X509Certificate) certO;

                    final boolean verify = signer.verify(cert, provider);
                    if (!verify)
                        return false;

                    ret = true;
                }
            } catch (Exception e) {
                Logger.error("CMS verify: %s", e);
                return false;
            }
        }

        return ret;
    }

    public static byte[] sha256(AttachmentMessage msg) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dt = sdf.parse(msg.getTime());
        final long time = dt.getTime() / 1000L;
        final byte[] key = msg.getAssetKey().getBytes(StandardCharsets.UTF_16BE);

        ByteBuffer bb = ByteBuffer.allocate(8 + key.length);
        bb.put(key);
        bb.putLong(time);

        MessageDigest md = MessageDigest.getInstance("SHA256");
        md.update(bb.array());
        return md.digest();
    }
}
