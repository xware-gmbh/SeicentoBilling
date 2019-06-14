appstage=$1
if [ "$1" == "" ]; then
   appstage="PROD"
fi
echo "Starting Applikation on " $appstage

docker run -itd \
-e APP_STAGE=$appstage \
-e APP_NAME='Billing' \
-p 19080:8080 \
-p 19443:8443 \
--name billing \
dockregxwr-on.azurecr.io/seicentobilling