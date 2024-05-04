public class BuildDownloadScript {
    public static void main(String[] args) {
        // 1st line
        System.out.println("curl 'https://oda.ft.dk/api/SagDokument?$expand=Sag,Dokument/Fil&$filter=Sag/typeid%20eq%2010%20and%20Dokument/dato%20ge%20datetime%272022-01-01T00:00:00%27&$orderby=Dokument/dato&$inlinecount=allpages' > file1.json");

        int fileNumber = 2;
        String baseLine = "curl 'https://oda.ft.dk/api/SagDokument?$expand=Sag,Dokument/Fil&$filter=Sag/typeid%20eq%2010%20and%20Dokument/dato%20ge%20datetime%272022-01-01T00:00:00%27&$orderby=Dokument/dato&$inlinecount=allpages&$skip=";
        for (int pagination = 100; pagination < 4541; pagination += 100)
        {
            StringBuilder sb = new StringBuilder(baseLine);
            sb.append(pagination);
            sb.append("' > file");
            sb.append(fileNumber++);
            sb.append(".json");
            System.out.println(sb.toString());
        }
    }
}