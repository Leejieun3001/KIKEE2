const pool = require('../config/db_pool');
const router = require('express').Router();
const bcrypt = require('bcrypt-nodejs');
const async = require('async');
const globalModule = require('../module/globalModule');
const nodemailer = require('nodemailer');
const aws = require('aws-sdk');
const multer = require('multer');
const multerS3 = require('multer-s3');
aws.config.loadFromPath('./config/aws_config.json');
const s3 = new aws.S3();
const upload = multer({
    storage: multerS3({
        s3: s3,
        bucket: 'sungshinproject',
        acl: 'public-read',
        key: function (req, file, cb) {
            cb(null, Date.now() + '.' + file.originalname.split('.').pop())
        }
    })
});

/**
 * api 목적        : 단어 데이터 전송
 * request params : { String category: "카테고리"
 *                    }
 */
router.get('/', function (req, res) {

    let resultJson = {
        message: 'SUCCESS',
        words: []
    };

    let selectWords = function (connection, callback) {
        connection.query("SELECT * FROM sungshinDB.Word where category = ? order by rand() limit 10 ", req.query.category, function (error, rows) {
            if (error) callback(error, connection, "Selecet query Error : ");
            else {
                if (rows.length === 0) {
                    // 존재하는 아이디가 없는 경우
                    res.status(200).send({ message: "WORD_NOT_EIXT" });
                    callback("ALREADY_SEND_MESSAGE", connection, "api : /words/");
                } else {

                    for (var x in rows) {
                        var word = {}
                        word.korea = rows[x].korea;
                        word.english = rows[x].english;
                        resultJson.words.push(word);
                    }
                    res.status(200).send(resultJson)
                    callback(null, connection,"api : /words/");
                }
            }
        });
    }


    var task = [globalModule.connect.bind(this), selectWords, globalModule.releaseConnection.bind(this)];
    async.waterfall(task, globalModule.asyncCallback.bind(this));
});




module.exports = router;