package comp5216.sydney.edu.au.learn.Common;

public class lectureHistoryDTO {
    private String coverImageUrl;
    private String pdfTitle;


    public lectureHistoryDTO(String coverImageUrl, String pdfTitle) {
        this.coverImageUrl = coverImageUrl;
        this.pdfTitle = pdfTitle;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getPdfTitle() {
        return pdfTitle;
    }

    public void setPdfTitle(String pdfTitle) {
        this.pdfTitle = pdfTitle;
    }
}
