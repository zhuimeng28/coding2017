/**********************************************************************************************************************
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.                                       *
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.                        *
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.                                                   *
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.                     *
 * Vestibulum commodo. Ut rhoncus gravida arcu.                                                                       *
 **********************************************************************************************************************/

package com.coderising.ood.srp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PromotionMail
{
    private static final String        NAME_KEY      = "NAME";
    private static final String        EMAIL_KEY     = "EMAIL";
    private              Configuration config        = new Configuration();
    private              ProductInfo   productInfo   = new ProductInfo();
    private              FileUtil      fileUtil      = new FileUtil();
    private              boolean       emailDebug    = false;
    private              String        sendMailQuery = null;
    private              String        smtpHost      = null;
    private              String        altSmtpHost   = null;
    private              String        fromAddress   = null;
    private              String        toAddress     = null;
    private              String        subject       = null;
    private              String        message       = null;

    public PromotionMail( File file, boolean mailDebug ) throws Exception
    {
        this.emailDebug = mailDebug;
        readProductInfos( file );
        configuringEMAILSetting();
        List mailingList = queryMailingList();
        sendEMails( mailDebug, mailingList );
    }

    private void readProductInfos( File file ) throws IOException
    {//读取配置文件， 文件中只有一行用空格隔开， 例如 P8756 iPhone8
        String[] productInfos = fileUtil.readFile( file );
        productInfo.setProductID( productInfos[ 0 ] );
        productInfo.setProductDesc( productInfos[ 1 ] );
        System.out.println( "产品ID = " + productInfo.getProductID() + "\n" );
        System.out.println( "产品描述 = " + productInfo.getProductDesc() + "\n" );
    }

    private void configuringEMAILSetting()
    {
        setSMTPHost();
        setAltSMTPHost();
        setFromAddress();
    }

    private List queryMailingList() throws Exception
    {
        setLoadQuery();
        return loadMailingList();
    }

    protected void sendEMails( boolean debug, List mailingList ) throws IOException
    {
        System.out.println( "开始发送邮件" );
        if ( mailingList != null )
        {
            Iterator iter = mailingList.iterator();
            while ( iter.hasNext() )
            {
                configureEMail( ( HashMap ) iter.next() );
                try
                {
                    if ( toAddress.length() > 0 )
                    {
                        MailUtil.sendEmail( toAddress, fromAddress, subject, message, smtpHost, debug );
                    }
                }
                catch ( Exception e )
                {
                    try
                    {
                        MailUtil.sendEmail( toAddress, fromAddress, subject, message, altSmtpHost, debug );
                    }
                    catch ( Exception e2 )
                    {
                        System.out.println( "通过备用 SMTP服务器发送邮件失败: " + e2.getMessage() );
                    }
                }
            }
        }
        else
        {
            System.out.println( "没有邮件发送" );
        }

    }

    protected void setSMTPHost()
    {
        smtpHost = config.getProperty( ConfigurationKeys.SMTP_SERVER );
    }

    protected void setAltSMTPHost()
    {
        altSmtpHost = config.getProperty( ConfigurationKeys.ALT_SMTP_SERVER );
    }

    protected void setFromAddress()
    {
        fromAddress = config.getProperty( ConfigurationKeys.EMAIL_ADMIN );
    }

    protected void setLoadQuery() throws Exception
    {
        sendMailQuery = "Select name from subscriptions " + "where product_id= '" + productInfo
                .getProductID() + "' " + "and send_mail=1 ";
        System.out.println( "loadQuery set" );
    }

    protected List loadMailingList() throws Exception
    {
        return DBUtil.query( this.sendMailQuery );
    }

    protected void configureEMail( HashMap userInfo ) throws IOException
    {
        toAddress = ( String ) userInfo.get( EMAIL_KEY );
        if ( toAddress.length() > 0 )
        {
            setMessage( userInfo );
        }
    }

    protected void setMessage( HashMap userInfo ) throws IOException
    {
        String name = ( String ) userInfo.get( NAME_KEY );
        subject = "您关注的产品降价了";
        message = "尊敬的 " + name + ", 您关注的产品 " + productInfo.getProductDesc() + " 降价了，欢迎购买!";
    }

    public static void main( String[] args ) throws Exception
    {
        File productPromotionFile = new File(
                "D:\\02_workspace\\myproject\\coding2017\\students\\511134962\\ood-assignment\\src\\main\\java\\com\\coderising\\ood\\srp\\product_promotion.txt" );
        boolean       emailDebug = false;
        PromotionMail pe         = new PromotionMail( productPromotionFile, emailDebug );
    }


}
